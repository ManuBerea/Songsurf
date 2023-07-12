import re

from flask import request
from flask_jwt_extended import jwt_required, get_jwt_identity
from flask_restful import Resource
from werkzeug.security import check_password_hash, generate_password_hash

from app import db, api
from app.models import User

PASSWORD_PATTERN = re.compile(r'^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$')


def validate_password(password):
    if PASSWORD_PATTERN.fullmatch(password):
        return True
    return False


class Logout(Resource):
    @jwt_required()
    def post(self):
        return {"message": "Successfully signed out"}, 200


class DeleteAccount(Resource):
    @jwt_required()
    def delete(self):
        current_user_id = get_jwt_identity()
        user = User.query.filter_by(id=current_user_id).first()

        if not user:
            return {"message": "User not found."}, 404

        db.session.delete(user)
        db.session.commit()
        return {"message": "Account deleted successfully."}, 200


class ChangeUsername(Resource):
    @jwt_required()
    def put(self):
        data = request.get_json()
        new_username = data.get("new_username")

        if not new_username:
            return {"message": "New username not provided."}, 400

        if User.query.filter_by(username=new_username).first():
            return {"message": "Username already in use."}, 400

        current_user_id = get_jwt_identity()
        user = User.query.filter_by(id=current_user_id).first()

        if not user:
            return {"message": "User not found."}, 404

        user.username = new_username
        db.session.commit()

        return {"message": "Username changed successfully."}, 200


class ChangeEmail(Resource):
    @jwt_required()
    def put(self):
        data = request.get_json()
        new_email = data.get("new_email")
        password = data.get("password")

        if not new_email or not password:
            return {"message": "New email or password not provided."}, 400

        if User.query.filter_by(email=new_email).first():
            return {"message": "Email already in use."}, 400

        current_user_id = get_jwt_identity()
        user = User.query.filter_by(id=current_user_id).first()

        if not user:
            return {"message": "User not found."}, 404

        if not check_password_hash(user.password, password):
            return {"message": "Password is incorrect."}, 401

        user.email = new_email
        db.session.commit()

        return {"message": "Email changed successfully."}, 200


class ChangePassword(Resource):
    @jwt_required()
    def put(self):
        data = request.get_json()
        current_password = data.get("current_password")
        new_password = data.get("new_password")
        repeat_new_password = data.get("repeat_new_password")

        if not current_password or not new_password or not repeat_new_password:
            return {"message": "Current password or new password or repeated new password not provided."}, 400

        if new_password != repeat_new_password:
            return {"message": "New password and repeated new password do not match."}, 400

        if not validate_password(new_password):
            return {"message": "New password does not meet the complexity requirements."}, 400

        current_user_id = get_jwt_identity()
        user = User.query.filter_by(id=current_user_id).first()

        if not user:
            return {"message": "User not found."}, 404

        if not check_password_hash(user.password, current_password):
            return {"message": "Current password is incorrect."}, 401

        user.password = generate_password_hash(new_password)
        db.session.commit()

        return {"message": "Password changed successfully."}, 200


class GetUsername(Resource):
    @jwt_required()
    def get(self):
        current_user_id = get_jwt_identity()
        user = User.query.filter_by(id=current_user_id).first()

        if not user:
            return {"message": "User not found."}, 404

        return {"username": user.username}, 200


api.add_resource(Logout, "/logout")
api.add_resource(DeleteAccount, "/delete_account")
api.add_resource(ChangeUsername, "/change_username")
api.add_resource(ChangeEmail, "/change_email")
api.add_resource(ChangePassword, "/change_password")
api.add_resource(GetUsername, "/get_username")
