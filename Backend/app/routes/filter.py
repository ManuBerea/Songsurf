from flask import jsonify, request
from flask_jwt_extended import jwt_required
from flask_restful import Resource

from app import api
from app.models import Song


class DynamicFilterData(Resource):
    @jwt_required()
    def post(self):
        posted_data = request.get_json()

        selected_genres = posted_data.get('genres', [])
        selected_languages = posted_data.get('languages', [])
        selected_years = posted_data.get('years', [])

        query = Song.query

        if selected_genres:
            query = query.filter(Song.genre.in_(selected_genres))
        if selected_languages:
            query = query.filter(Song.language.in_(selected_languages))
        if selected_years:
            query = query.filter(Song.year.in_(selected_years))

        remaining_genres = query.with_entities(Song.genre).distinct().all()
        remaining_languages = query.with_entities(Song.language).distinct().all()
        remaining_years = query.with_entities(Song.year).distinct().all()

        response = {
            'genres': [genre[0] for genre in remaining_genres],
            'languages': [language[0] for language in remaining_languages],
            'years': [year[0] for year in remaining_years],
        }
        return jsonify(response)


api.add_resource(DynamicFilterData, "/filter/dynamic")
