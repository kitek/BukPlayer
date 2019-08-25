const path = require('path');
const readlineSync = require('readline-sync');
const bookToScraper = require('./scrapers/bookToScraper.js');
const googleBooksScraper = require('./scrapers/googleBooksScraper.js');
const clear = require('clear');
const download = require('image-downloader');
const { coverExtensions } = require('./constants.js');

module.exports = {

    findBookMeta : async (bookFile) => {
        const query = bookFile.path.replace('-', '').replace('  ', ' ').trim();

        const meta = await Promise.all([
            bookToScraper(query),
            googleBooksScraper(query)
        ]);
        const results = meta.filter((proposal) => {
            return Array.isArray(proposal) && proposal.length > 0;
        });
        const proposal = [].concat.apply([], results);

        if(bookFile.coverPath.length > 0) {
            proposal.unshift({
                coverPath: bookFile.path + path.sep + bookFile.coverPath
            });
        }

        return proposal;
    },

    findBookCover: async (bookInfo) => {
        const isRemoteCover = bookInfo.coverPath.startsWith("http://") || bookInfo.coverPath.startsWith("https://")
        if(!isRemoteCover) return bookInfo.coverPath;

        const coverSourcePath = bookInfo.coverPath;
        const coverDestPath = bookInfo.path + path.sep + 'cover.jpg';
        const options = {
            url: coverSourcePath,
            dest: coverDestPath
        };

        await download.image(options);

        return coverDestPath;
    }

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
        let coverPath = (proposal.find((item) => {
            return item.coverPath.length > 0 && (item.title === book.title);
        }) || {}).coverPath || '';

        if(coverPath.length === 0) {
            // try find cover using author
            coverPath = (proposal.find((item) => {
                return item.coverPath.length > 0 && (item.author === book.author);
            }) || {}).coverPath || '';
        }

        if(coverPath.length === 0) return '';

        const coverRelativePath = book.path + path.sep + 'cover.jpg';
        const options = {
            url: coverPath,
            dest: coverRelativePath
        }
        const { filename, image } = await download.image(options)

        return coverRelativePath;
    } catch(e) {
        console.error('getCoverPath', e);
        return '';
    }
}
