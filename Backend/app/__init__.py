from flask import Flask
from flask_jwt_extended import JWTManager
from flask_restful import Api
from flask_sqlalchemy import SQLAlchemy


def create_app():
    app = Flask(__name__)
    app.config['SQLALCHEMY_DATABASE_URI'] = 'postgresql://postgres:gogogiu7@localhost:5432/songsurf'
    app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False  # silence the deprecation warning
    app.config["JWT_SECRET_KEY"] = "your_secret_key"
    jwt = JWTManager(app)
    db = SQLAlchemy(app)
    api = Api(app)
    return app, jwt, db, api


app, jwt, db, api = create_app()

from app.routes import authentication
from app.routes import account
from app.routes import song
from app.routes import filter
from app.routes import playlist
from app.routes import reccomandation_system
from app.utils import song_to_dict
