from bs4 import BeautifulSoup
from selenium import webdriver


class ArtistScraper:
    def __init__(self):
        pass

    def getHTMLdocument(self, url):
        driver = webdriver.Chrome(executable_path='C:\\Windows\\webdrivers\\chromedriver.exe')
        driver.get(url)
        html = driver.page_source
        driver.quit()
        return html

    def scrape_data(self, items):
        artist_names = []
        for item in items:
            name = item.find("div", {"class": "td-contents"}).find("a").text.strip()
            artist_names.append(name)
        return artist_names

    def get_artist_details(self, url):
        html_document = self.getHTMLdocument(url)
        soup = BeautifulSoup(html_document, 'html.parser')
        items = soup.find('article', {'id': 'genre-artists-table'}).find_all('tr')
        artist_names = self.scrape_data(items[1:])
        return artist_names
