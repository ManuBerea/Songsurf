
class SongToDict:
    def __init__(self):
        self.playlist_id = None
        self.rating = None
        self.year = None
        self.language = None
        self.genre = None
        self.artist_name = None
        self.title = None
        self.youtube_song_id = None
        self.id = None

    def song_to_dict(self):
        song_dict = {
            "id": self.id,
            "youtube_song_id": self.youtube_song_id,
            "title": self.title,
            "artist_name": self.artist_name,
            "genre": self.genre,
            "language": self.language,
            "year": self.year,
            "rating": self.rating,
            "playlist_id": self.playlist_id
        }
        return song_dict
