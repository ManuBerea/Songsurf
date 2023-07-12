import random
from collections import defaultdict

import numpy as np
import pandas as pd
from flask import request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_restful import Resource
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.metrics.pairwise import cosine_similarity
from sqlalchemy.exc import IntegrityError

from app import api, db, app
from app.models import Song, UserSongPreference, RecommendedSong
from app.utils.song_to_dict import SongToDict


class SongPreference(Resource):
    @jwt_required()
    def post(self):
        current_user_id = get_jwt_identity()

        song_id = request.json.get('song_id')
        liked = request.json.get('liked')

        if song_id is None or liked is None:
            return {'error': 'Invalid request. song_id and liked are required.'}, 400

        song = Song.query.get(song_id)
        if not song:
            return {'error': 'Song not found.'}, 404

        preference = UserSongPreference(user_id=current_user_id, song_id=song_id, liked=liked)

        try:
            db.session.add(preference)
            db.session.commit()
        except IntegrityError:
            return {'error': 'This preference already exists.'}, 400

        return {'message': 'Preference recorded.'}, 201


def get_songs():
    return Song.query.all()


first_request_made = False


@jwt_required()
def delete_previous_recommendations():
    current_user_id = get_jwt_identity()
    RecommendedSong.query.filter_by(user_id=current_user_id).delete()
    db.session.commit()


@jwt_required()
def delete_previous_user_preferences():
    current_user_id = get_jwt_identity()
    UserSongPreference.query.filter_by(user_id=current_user_id).delete()
    db.session.commit()


class RecommendSong(Resource):
    @staticmethod
    def get_all_user_preferences(current_user_id):
        all_user_preferences = UserSongPreference.query.filter_by(user_id=current_user_id).all()

        return all_user_preferences

    @staticmethod
    def check_three_dislikes_in_row(user_preferences):
        if len(user_preferences) < 3:
            return False
        return all(not pref.liked for pref in user_preferences[-3:])

    @staticmethod
    def get_artists_count(user_preferences):
        artists_count = defaultdict(int)
        for preference in user_preferences:
            song = Song.query.get(preference.song_id)
            artists_count[song.artist_name] += 1

        return artists_count

    @jwt_required()
    def get(self, songs=get_songs()):
        try:
            current_user_id = get_jwt_identity()

            global first_request_made
            if not first_request_made:
                delete_previous_recommendations()
                delete_previous_user_preferences()
                first_request_made = True

            all_user_preferences = self.get_all_user_preferences(current_user_id)

            artists_count = self.get_artists_count(all_user_preferences)

            recommended_songs = RecommendedSong.query.filter_by(user_id=current_user_id).all()
            recommended_song_ids = [song.song_id for song in recommended_songs]

            unrated_songs = [song for song in songs if song.id not in recommended_song_ids]

            if not all_user_preferences:
                if unrated_songs:
                    random_song = random.choice(unrated_songs)
                    return jsonify(SongToDict.song_to_dict(random_song))
                else:
                    return {'error': 'All songs have been recommended.'}, 404

            if self.check_three_dislikes_in_row(all_user_preferences):
                random_song = random.choice(unrated_songs)
                return jsonify(SongToDict.song_to_dict(random_song))

            liked_song_ids = [preference.song_id for preference in all_user_preferences if preference.liked]
            disliked_song_ids = [preference.song_id for preference in all_user_preferences if not preference.liked]

            songs_df = pd.DataFrame([{
                'id': song.id,
                'artist_name': song.artist_name,
                'genre': song.genre,
                'language': song.language,
                'year': song.year
            } for song in unrated_songs])

            songs_df['combined_features'] = songs_df['genre'] + ' ' + songs_df['genre'] + ' ' + songs_df[
                'language'] + ' ' + songs_df['language'] + ' ' + songs_df['year'].astype(str)

            tfidf = TfidfVectorizer()
            tfidf_matrix = tfidf.fit_transform(songs_df['combined_features'])

            similarity_scores = np.zeros(tfidf_matrix.shape[0])

            for i, song_id in enumerate(liked_song_ids):
                idx = songs_df[songs_df['id'] == song_id].index
                if idx.empty:
                    continue
                liked_song_vector = tfidf_matrix[idx[0]]
                cosine_sim = cosine_similarity(liked_song_vector, tfidf_matrix)
                decay_factor = (5 - i) / 5
                similarity_scores += 10 * decay_factor * cosine_sim.flatten()

            for i, song_id in enumerate(disliked_song_ids):
                idx = songs_df[songs_df['id'] == song_id].index
                if idx.empty:
                    continue
                disliked_song_vector = tfidf_matrix[idx[0]]
                cosine_dissim = 1 - cosine_similarity(disliked_song_vector, tfidf_matrix)
                decay_factor = (5 - i) / 5
                similarity_scores -= 50 * decay_factor * cosine_dissim.flatten()

            songs_df['similarity_score'] = similarity_scores
            songs_df = songs_df.sort_values('similarity_score', ascending=False)

            songs_df = songs_df[~songs_df['id'].isin(liked_song_ids + disliked_song_ids)]
            songs_df = songs_df[~songs_df['id'].isin(recommended_song_ids)]
            songs_df = songs_df[~songs_df['artist_name'].isin(artists_count.keys())]

            if not songs_df.empty:
                recommended_song_id = int(songs_df.iloc[0]['id'])
                recommended_song = Song.query.get(recommended_song_id)

                new_recommendation = RecommendedSong(user_id=current_user_id, song_id=recommended_song.id)
                db.session.add(new_recommendation)
                db.session.commit()

                return jsonify(SongToDict.song_to_dict(recommended_song))

            return {'error': 'No songs could be recommended.'}, 404

        except Exception as e:
            app.logger.error(f"Unexpected error: {e}")
            return {'error': 'An unexpected error occurred.'}, 500


api.add_resource(RecommendSong, "/song/recommend")
api.add_resource(SongPreference, "/song/preference")
