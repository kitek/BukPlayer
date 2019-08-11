const rp = require('request-promise');

module.exports = (query, limit = 2) => {
    const url = 'https://www.googleapis.com/books/v1/volumes?q=' + encodeURIComponent(query);

    return rp({ uri: url, json: true }).then((json) => {
        let books = [];
        let results = json.items.slice(0, limit) || [];

        results.forEach((element) => {
            const coverPath = getThumbnail(element);
            const title = element.volumeInfo.title || '';
            const author = element.volumeInfo.authors[0] || '';
            const description = element.volumeInfo.description || '';

            books.push({ title, coverPath, author, description });
        });

        return books;
    });

    function getThumbnail(element) {
        try {
            return element.volumeInfo.imageLinks.thumbnail || '';
        } catch(e) {
            return ''
        }
    }
}
