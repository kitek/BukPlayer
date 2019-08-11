const path = require('path');
const readlineSync = require('readline-sync');
const bookToScraper = require('./scrapers/bookToScraper.js');
const googleBooksScraper = require('./scrapers/googleBooksScraper.js');
const clear = require('clear');
const download = require('image-downloader');

module.exports.findMeta = async (books) => {
    const size = (books || []).length;

    console.log('Found ' + size + ' new books.')

    for(let i = 0; i < size; i+=1) {
        const book = books[i];
        const query = book.path;
        const results = await Promise.all([
            bookToScraper(query),
            googleBooksScraper(query)
        ]);
        const proposal = [].concat.apply([], results);

        clear();
        console.log('[' + (i + 1) + '/' + books.length + '] Directory: "' + query + '"');

        book.author = ask(proposal, 'author');

        clear();
        console.log('[' + (i + 1) + '/' + books.length + '] Directory: "' + query + '"');

        book.title = ask(proposal, 'title');

        clear();
        console.log('[' + (i + 1) + '/' + books.length + '] Directory: "' + query + '"');

        book.description = ask(proposal, 'description');
        book.coverPath = await getCoverPath(book, proposal);
    }

    return books;

};

function ask(proposals, property) {
    const options = proposals.map((item) => {
        return item[property].trim().replace("\n", " ");
    }).filter((item) => {
        return item.length > 0;
    }).reduce((unique, item) => {
        return unique.includes(item) ? unique : [...unique, item]
    }, []);

    const selectedIndex = readlineSync.keyInSelect(options, 'Which ' + property + '?');

    return options[selectedIndex] || '';
}

async function getCoverPath(book, proposal) {
    if(book.coverPath.length > 0) return book.coverPath;

    try {
        const coverPath = proposal.find((item) => {
            return item.coverPath.length > 0 && (item.title === book.title);
        }).coverPath || '';

        if(coverPath.length === 0) return '';

        const coverRelativePath = book.path + path.sep + 'cover.jpg';
        const options = {
            url: coverPath,
            dest: coverRelativePath
        }
        const { filename, image } = await download.image(options)

        return coverRelativePath;
    } catch(e) {
        return '';
    }
}
