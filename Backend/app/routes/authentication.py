import re

from flask import request
from flask_jwt_extended import create_access_token
from flask_restful import Resource
from werkzeug.security import check_password_hash
from werkzeug.security import generate_password_hash

from app import db, api
from app.models import User

PASSWORD_PATTERN = re.compile(r'^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$')


def validate_password(password):
    if PASSWORD_PATTERN.fullmatch(password):
        return True
    return False


class Register(Resource):
    def post(self):
        data = request.get_json()
        username = data["username"]
        email = data["email"]
        password = data["password"]

        if not validate_password(password):
            return {"message": "Password must be at least 8 characters, include an uppercase letter, a lowercase "
                               "letter, a digit, and a special character."}, 400

        hashed_password = generate_password_hash(password)

        if User.query.filter_by(email=email).first():
            return {"message": "Email already in use."}, 400

        if User.query.filter_by(username=username).first():
            return {"message": "Username already in use."}, 400

        user = User(username=username, email=email, password=hashed_password)
        db.session.add(user)
        db.session.commit()

        return {"message": "User created successfully."}, 201


class Login(Resource):
    def post(self):
        data = request.get_json()
        email = data["email"]
        password = data["password"]
        user_email = User.query.filter_by(email=email).first()

        if user_email and check_password_hash(user_email.password, password):
            access_token = create_access_token(identity=user_email.id)
            return {"access_token": access_token}, 200
        else:
            return {"message": "Invalid credentials."}, 401


api.add_resource(Register, "/register")
api.add_resource(Login, "/login")

