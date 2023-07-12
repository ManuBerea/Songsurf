from datetime import datetime

from sqlalchemy.ext.associationproxy import association_proxy
from sqlalchemy.orm import backref

from app import db

db.metadata.clear()


class User(db.Model):
    __tablename__ = "users"
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(80), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(120), nullable=False)

    def __repr__(self):
        return f"{self.username} {self.email} {self.password} {self.id}"


class Song(db.Model):
    __tablename__ = "songs"
    id = db.Column(db.Integer, primary_key=True)
    youtube_song_id = db.Column(db.String(80), unique=True, nullable=False)
    title = db.Column(db.String(200), nullable=False)
    genre = db.Column(db.String(80), nullable=False)
    year = db.Column(db.Integer, nullable=False)
    language = db.Column(db.String(80), nullable=False)
    artist_name = db.Column(db.String(80), nullable=False)
    rating = db.Column(db.Integer, nullable=True)
    playlist_id = db.Column(db.Integer, db.ForeignKey('playlists.id'), nullable=True)
    playlists = association_proxy('songs_playlists', 'playlist')

    def __repr__(self):
        return f"{self.title} {self.genre} {self.year} {self.language} {self.rating} {self.id} {self.playlist_id} {self.youtube_song_id}"


class UserSongPreference(db.Model):
    __tablename__ = 'user_song_preferences'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    song_id = db.Column(db.Integer, db.ForeignKey('songs.id'), nullable=False)
    liked = db.Column(db.Boolean, nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False, default=datetime.utcnow)

    def __init__(self, user_id, song_id, liked):
        self.user_id = user_id
        self.song_id = song_id
        self.liked = liked
        self.timestamp = datetime.utcnow()


class RecommendedSong(db.Model):
    __tablename__ = 'recommended_song'

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)
    song_id = db.Column(db.Integer, db.ForeignKey('songs.id'), nullable=False)


class Playlist(db.Model):
    __tablename__ = "playlists"
    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(80), nullable=False)
    user_id = db.Column(db.Integer, db.ForeignKey('users.id'), nullable=False)

    user = db.relationship("User", backref="playlists", lazy=True)
    songs = db.relationship('SongsPlaylists', backref='parent_playlist', lazy=True)

    def __repr__(self):
        return f"{self.name} {self.id} {self.user_id}"


class SongsPlaylists(db.Model):
    __tablename__ = 'songs_playlists'
    song_id = db.Column(db.Integer, db.ForeignKey('songs.id'), primary_key=True)
    playlist_id = db.Column(db.Integer, db.ForeignKey('playlists.id'), primary_key=True)
    timestamp = db.Column(db.DateTime, default=datetime.utcnow)

    song = db.relationship("Song", backref=backref("songs_playlists", cascade="all, delete-orphan"))
    playlist = db.relationship("Playlist", backref=backref("songs_in_playlists", cascade="all, delete-orphan"))

    def __repr__(self):
        return f"{self.song_id} {self.playlist_id}"


db.create_all()
