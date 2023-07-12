import os
from collections import Counter
from html import unescape

import requests

from app import db
from app.models import Song
from app.utils.artist_names_scraper import ArtistScraper


def find_most_frequent_and_percentage(string_list):
    count_dict = Counter(string_list)
    print(string_list)
    most_frequent_string, max_count = count_dict.most_common(1)[0]
    percentage = (max_count / len(string_list)) * 100

    return most_frequent_string, percentage


class SongInfoProvider:

    def __init__(self):
        self.api_key = os.environ.get("YOUTUBE_API_KEY")
        self.snippet = "snippet"
        self.video_category = "10"
        self.type = "video"
        self.max_results_channel_ids = "10"
        self.max_results = "50"
        self.video_definition = "high"
        self.video_duration = "short"
        self.order = "viewCount"
        self.genre = "pop"

    def get_artist_names(self):
        scraper = ArtistScraper()
        artist_names = []

        try:
            url = f"https://www.chosic.com/genre-chart/{self.genre}/"
            artist_names = scraper.get_artist_details(url)

            artist_names = artist_names[:50]

            print(artist_names)
        except Exception as e:
            print(f"Exception: {e}")
        return artist_names

    def get_channel_info(self, artist_name):
        url = f"https://www.googleapis.com/youtube/v3/search?part={self.snippet}&maxResults={self.max_results_channel_ids}" \
              f"&type={self.type}&videoCategoryId={self.video_category}&videoDefinition={self.video_definition}" \
              f"\&videoDuration={self.video_duration}&key={self.api_key}&q={artist_name}"

        try:
            response = requests.get(url)
            if response.status_code == 200:
                data = response.json()
                channel_ids = []
                for item in data["items"]:
                    channel_id = item["snippet"]["channelId"]
                    channel_title = item["snippet"]["channelTitle"]
                    artist_name_modified = artist_name.replace(" ", "")
                    channel_title = channel_title.replace(" ", "")
                    if artist_name_modified.lower() in channel_title.lower():
                        channel_ids.append(channel_id)

                if channel_ids:
                    most_frequent_id, percentage = find_most_frequent_and_percentage(channel_ids)
                    if percentage >= 50:
                        print(f"Most frequent channel id: {most_frequent_id} with percentage: {percentage}"
                              f" for artist: {artist_name}")
                        return most_frequent_id, artist_name

            else:
                error_json = response.json()
                if error_json["error"]["errors"][0]["reason"] == "quotaExceeded":
                    print("Quota exceeded!")
                else:
                    print(f"Error: {error_json}")
                exit(1)

        except Exception as e:
            print(f"Exception: {e}")
            exit(1)

        return None

    def get_song_info(self, channel_id, artist_name):
        url = f"https://www.googleapis.com/youtube/v3/search?part={self.snippet}&maxResults={self.max_results}" \
              f"&type={self.type}&videoCategoryId={self.video_category}&videoDefinition={self.video_definition}" \
              f"&videoDuration={self.video_duration}&key={self.api_key}&order={self.order}&channelId={channel_id}"
        try:
            response = requests.get(url)
            if response.status_code == 200:
                data = response.json()

                for item in data["items"]:
                    song_id = item["id"]["videoId"]
                    title = unescape(item["snippet"]["title"])
                    year = item["snippet"]["publishedAt"][:4]
                    language = "italian"
                    genre = "r&b"

                    song = Song.query.filter_by(youtube_song_id=song_id).first()
                    if song:
                        continue
                    if artist_name.lower() not in title.lower():
                        continue
                    excluded_words = ["trailer", "#", "shorts", "cover", "remix", "episode", "teaser", "trailer",
                                      "reaction", "review", "interview", "clip", "movie", "film", "preview", "part",
                                      "scene", "unboxing", "edition", "behind the", "commentary", "promo", "tour",
                                      "announce", "session", "intro", "coming", "making of", "short", "best of",
                                      "tv show", "intro", "outro", "discuss", "out now", "snippet", "ep.", "backstage",
                                      "vlog", "webisode", "ask:reply", "talks", "speaks", "vevo lift", "watch this",
                                      "hare hare", "extra", "fuse news", "tonight show", "speech", "247hh", "pt.",
                                      "track by track", "instrumental", "commercial", "freestyle", " fan ", "vinyl",
                                      "anniversary", "available", "vertical video", "recap", "highlight", "sample",
                                      "news", "breaking", "demo", "documentary", "challenge", "interlude", "spoiler",
                                      "diary", "mashup", "message"]
                    if any(word in title.lower() for word in excluded_words):
                        continue

                    song = Song(youtube_song_id=song_id, title=title, genre=genre, year=year, language=language,
                                artist_name=artist_name)
                    db.session.add(song)
                    db.session.commit()

            else:
                error_json = response.json()
                if error_json["error"]["errors"][0]["reason"] == "quotaExceeded":
                    print("Quota exceeded!")
                else:
                    print(f"Error: {error_json}")

                exit(1)

        except Exception as e:
            print(f"Exception: {e}")
            exit(1)


if __name__ == '__main__':
    provider = SongInfoProvider()
    artists = provider.get_artist_names()

    for artist in artists:
        channel_info = provider.get_channel_info(artist)
        if channel_info:
            channel_id, artist_name = channel_info
            if channel_id and artist_name:
                provider.get_song_info(channel_id, artist_name)
