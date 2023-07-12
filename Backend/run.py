from app import app
import sys
if __name__ == '__main__':
    print(sys.executable)
    app.run(host='localhost', threaded=True)
