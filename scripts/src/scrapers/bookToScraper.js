const rp = require('request-promise');
const $ = require('cheerio');

module.exports = (query, limit = 2) => {
    const url = 'http://bookto.pl/szukaj/' + encodeURIComponent(query);
    return rp(url).then((html) => {
        const results = $('div[itemtype="http://schema.org/Book"]', html).slice(0, limit) || [];
        let books = [];

        results.each((index) => {
            const element = results[index];
            const coverPath = $('div.cover img[itemprop="image"]', element).attr('src') || '';
            const title = $('h3.title span[itemprop="name"]', element).text() || '';
            const author = $('span[itemtype="http://schema.org/Person"] a span', element).text() || '';
            const description = $('p[itemprop="description"]', element).text() || '';

            books.push({ title, coverPath, author, description });
        });

        return books;
    });
}
