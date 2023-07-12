from random import choice

from flask import jsonify, request
from flask_jwt_extended import jwt_required
from flask_restful import Resource

from app import api
from app.models import Song
from app.utils.song_to_dict import SongToDict


def get_songs():
    return Song.query.all()


class RandomSong(Resource):
    @jwt_required()
    def get(self, songs=get_songs()):

        if not songs:
            return {"error": "No songs found in the database"}, 404

        song = choice(songs)
        song_dict = SongToDict.song_to_dict(song)

        return jsonify(song_dict)


class SongByFilters(Resource):
    @jwt_required()
    def get(self):
        filter_params = {
            'genre': lambda q, value: q.filter(Song.genre.in_(value)),
            'language': lambda q, value: q.filter(Song.language.in_(value)),
            'year': lambda q, value: q.filter(Song.year.in_(value))
        }

        query = Song.query
        if len(request.args) == 0:
            songs = query.all()
            if not songs:
                return {"error": "No songs found in the database"}, 404
        else:
            for param in request.args.keys():
                if param in filter_params:
                    values = request.args.getlist(param)
                    values = [value.lower() for value in values]
                    query = filter_params[param](query, values)

            songs = query.all()

            if not songs:
                return {"error": "No songs found matching the provided filters."}, 404

        song = choice(songs)
        song_dict = SongToDict.song_to_dict(song)

        return jsonify(song_dict)


api.add_resource(RandomSong, "/song")
api.add_resource(SongByFilters, "/song/filter")
