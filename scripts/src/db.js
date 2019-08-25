const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const getMP3Duration = require('get-mp3-duration');
const { supportedExtensions, audioExtensions, coverExtensions } = require('./constants.js');

const indexFilePath = '.' + path.sep + 'index.json';

module.exports = {

    readDb: getSavedBooks,

    diff: (savedBooks, booksOnDisk) => {
        const savedBooksPath = savedBooks.items.map((item) => {
            return item.path;
        });
        const booksOnDiskPath = Object.keys(booksOnDisk);
        const booksToAdd = booksOnDiskPath.filter((path) => {
            return !savedBooksPath.includes(path)
        }).map((item) => {
            const coverPath = booksOnDisk[item].find((file) => coverExtensions.includes(path.extname(file))) || '';

            return {
                'path': item,
                'coverPath' : coverPath,
                'files': booksOnDisk[item].filter((file) => audioExtensions.includes(path.extname(file)))
            };
        });
        const booksToRemove = savedBooksPath.filter((path) => !booksOnDiskPath.includes(path));

        return { booksToAdd, booksToRemove };
    },

    findBooks: (dir = '.') => {
        return walk(dir, supportedExtensions);
    },

    getDuration: (file) => {
        const buffer = fs.readFileSync(file);
        const duration = getMP3Duration(buffer) / 1000;

        return Math.round(duration);
    },

    addBook: (bookInfo) => {
        const book = {
            id: crypto.createHash('md5').update(bookInfo.path).digest("hex"),
            title: bookInfo.title,
            author: bookInfo.author,
            path: bookInfo.path,
            coverPath: bookInfo.coverPath,
            description: bookInfo.description,
            filesCount: bookInfo.files.length,
            durationInSeconds: bookInfo.filesDuration.reduce((prev, curr) => prev + curr.duration, 0)
        };

        const savedBooks = getSavedBooks() || {};
        const items = savedBooks.items || [];
        items.push(book);

        module.exports.saveBooks(items);
    },

    addBookFiles: (bookInfo) => {
        const model = {
            'metadata': {
                'total': bookInfo.filesDuration.length,
                'updatedAt': new Date().toISOString()
            },
            'items': bookInfo.filesDuration
        };
        const bookPath = bookInfo.path + path.sep + 'book.json';

        fs.writeFileSync(bookPath, JSON.stringify(model), 'utf8');
    },

    saveBooks: (books) => {
        const model = {
            'metadata': {
                'total': (books || []).length,
                'updatedAt': new Date().toISOString()
            },
            'items': books || []
        };

        fs.writeFileSync(indexFilePath, JSON.stringify(model), 'utf8');
    },

    removeBooks: (books) => {
        const size = books.length;
        if(size === 0) return;

        const savedBooks = getSavedBooks();
        const indexesToRemove = [];

        for(let i = 0; i < books.length; i+=1) {
            const book = books[i];
            const bookIndex = savedBooks.items.findIndex((savedBook) => savedBook.path === book);
            if(bookIndex > -1) indexesToRemove.push(bookIndex);
        }

        indexesToRemove.reverse().forEach((index) => {
            savedBooks.items.splice(index, 1);
        });

        module.exports.saveBooks(savedBooks.items);
    }

};

function walk(dir, filterExt = []) {
    let results = {};
    const list = fs.readdirSync(dir)

    list.forEach(function(file) {
        const stat = fs.statSync(file);
        const isDirectory = stat && stat.isDirectory();

        if(isDirectory) {
            const filesInDir = fs.readdirSync(file);
            const supportedFiles = filesInDir.filter((value) => {
                const ext = path.extname(value);
                return filterExt.length == 0 || filterExt.includes(ext);
            });

            results[file] = supportedFiles;
        }
    });

    return results;
}

function getSavedBooks() {
    try {
        const indexFile = fs.readFileSync(indexFilePath);
        return JSON.parse(indexFile);
    } catch(e) {
        return { metadata: { total: 0 }, items: [] };
    }
}

