from flask import request, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_restful import Resource

from app import db, api
from app.models import Playlist, Song, SongsPlaylists
from app.utils.song_to_dict import SongToDict


class PlaylistCreate(Resource):
    @jwt_required()
    def post(self):
        user_id = get_jwt_identity()
        name = request.json.get('name')

        if not name:
            return {"error": "Name of the playlist is required"}, 400

        playlist = Playlist.query.filter_by(name=name, user_id=user_id).first()

        if playlist:
            return {"error": "Playlist with this name already exists"}, 400

        new_playlist = Playlist(name=name, user_id=user_id)
        db.session.add(new_playlist)
        db.session.commit()

        return {"message": "Playlist created successfully"}, 201


class AddToPlaylist(Resource):
    @jwt_required()
    def post(self, playlist_id):
        song_id = request.json.get('song_id')

        if not song_id:
            return {"error": "Song id is required"}, 400

        playlist = Playlist.query.get(playlist_id)
        song = Song.query.get(song_id)

        if not playlist or not song:
            return {"error": "Song or Playlist not found"}, 404

        if any(sp.song_id == song.id for sp in playlist.songs_in_playlists):
            return {"error": "Song already in this playlist"}, 400

        association = SongsPlaylists(song_id=song.id, playlist_id=playlist.id)
        db.session.add(association)
        db.session.commit()

        return {"message": "Song added to playlist successfully"}, 200


class GetAllPlaylists(Resource):
    @jwt_required()
    def get(self):
        user_id = get_jwt_identity()

        playlists = Playlist.query.filter_by(user_id=user_id).all()

        if not playlists:
            return {"error": "No playlists found for the user"}, 404

        playlists_dict = []
        empty_playlists_dict = []
        for playlist in playlists:
            playlist_songs = playlist.songs
            song_count = len(playlist_songs)

            if song_count > 0:
                playlists_dict.append({
                    "playlist_id": playlist.id,
                    "name": playlist.name,
                    "song_count": song_count,
                    "youtube_song_id": playlist_songs[0].song.youtube_song_id
                })
            else:
                empty_playlists_dict.append({
                    "playlist_id": playlist.id,
                    "name": playlist.name,
                    "song_count": song_count,
                    "youtube_song_id": None
                })

        playlists_dict.extend(empty_playlists_dict)
        return jsonify(playlists_dict)


class GetPlaylistSongs(Resource):
    @jwt_required()
    def get(self, playlist_id):
        user_id = get_jwt_identity()

        playlist = Playlist.query.get(playlist_id)

        if not playlist:
            return {"error": "Playlist not found"}, 404

        if playlist.user_id != user_id:
            return {"error": "You don't have permission to view this playlist"}, 403

        playlist_songs = playlist.songs
        songs = [SongToDict.song_to_dict(song.song) for song in playlist_songs]

        return jsonify(songs)


class DeletePlaylist(Resource):
    @jwt_required()
    def delete(self, playlist_id):
        user_id = get_jwt_identity()
        playlist = Playlist.query.get(playlist_id)

        if not playlist:
            return {"error": "Playlist not found"}, 404

        if playlist.user_id != user_id:
            return {"error": "You don't have permission to delete this playlist"}, 403

        db.session.delete(playlist)
        db.session.commit()

        return {"message": "Playlist deleted successfully"}, 200


class RenamePlaylist(Resource):
    @jwt_required()
    def put(self, playlist_id):
        user_id = get_jwt_identity()
        new_name = request.json.get('name')

        if not new_name:
            return {"error": "New name is required"}, 400

        playlist = Playlist.query.get(playlist_id)

        if not playlist:
            return {"error": "Playlist not found"}, 404

        if playlist.user_id != user_id:
            return {"error": "You don't have permission to rename this playlist"}, 403

        playlist.name = new_name
        db.session.commit()

        return {"message": "Playlist renamed successfully"}, 200


class RemoveSongFromPlaylist(Resource):
    @jwt_required()
    def delete(self, playlist_id, song_id):

        if not song_id:
            return {"error": "Song id is required"}, 400

        playlist = Playlist.query.get(playlist_id)
        song = Song.query.get(song_id)

        if not playlist or not song:
            return {"error": "Song or Playlist not found"}, 404

        if not any(sp.song_id == song.id for sp in playlist.songs_in_playlists):
            return {"error": "Song not in this playlist"}, 400

        association = SongsPlaylists.query.filter_by(song_id=song.id, playlist_id=playlist.id).first()
        db.session.delete(association)
        db.session.commit()

        return {"message": "Song removed from playlist successfully"}, 200


api.add_resource(GetAllPlaylists, "/playlists")
api.add_resource(PlaylistCreate, "/playlist")
api.add_resource(AddToPlaylist, "/playlist/<int:playlist_id>/add")

api.add_resource(GetPlaylistSongs, "/playlist/<int:playlist_id>/songs")
api.add_resource(RemoveSongFromPlaylist, '/playlist/<int:playlist_id>/remove_song/<int:song_id>')

api.add_resource(DeletePlaylist, "/playlist/<int:playlist_id>/delete")
api.add_resource(RenamePlaylist, "/playlist/<int:playlist_id>/rename")
