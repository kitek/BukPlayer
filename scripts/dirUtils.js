const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const { hashElement } = require('folder-hash');
const getMP3Duration = require('get-mp3-duration');
const readlineSync = require('readline-sync');

const indexFilePath = '.' + path.sep + 'index.json';
const audioExts = ['.mp3', '.ogg'];

module.exports = {

    findBooks: async (dir = '.') => {
        console.log('Scanning directories...');
        const files = walk(dir, ['.mp3', '.ogg', '.jpg', '.png']);
        const books = await mapToBooks(files);
        const savedBooks = getSavedBooks();
        const diff = calcBooksDiff(books, savedBooks);

        return diff;
   },

    addBooks: (books) => {
        if(books.length === 0) return;

        const savedBooks = getSavedBooks() || {}
        const items = savedBooks.items || [];

        module.exports.saveBooks(items.concat(books))
    },

    saveBooks: (books) => {
        console.log('Saving book...');
        const model = {
            'metadata': {
                'total': (books || []).length,
                'updatedAt': new Date().toISOString()
            },
            'items': books || []
        };
        fs.writeFileSync(indexFilePath, JSON.stringify(model), 'utf8');
        saveBooksFiles(books);
    },

    removeBooks: (books) => {
        const size = books.length;
        if(size === 0) return

        console.log('Found ' + size + ' book(s) to remove:\n');
        books.forEach((book) => {
            console.log('- ' + book.author + ' ' + book.title + '\n');
        });

        const answer = readlineSync.keyInYN('Would you like to remove it from the index?');
        if(answer) removeFromIndex(books);
    }

};

async function mapToBooks(files) {
    const dirNames = {};

    files.forEach(function(file) {
        const firstDirName = file.split(path.sep)[1];

        if (!dirNames.hasOwnProperty(firstDirName)) {
            dirNames[firstDirName] = [];
        }
        dirNames[firstDirName].push(file);
    });

    const books = [];
    const keys = Object.keys(dirNames);
    for(let i = 0; i < keys.length; i+=1) {
        const name = keys[i];
        const coverLocalPath = dirNames[name].find((file) => {
            return ['.jpg', '.png'].includes(path.extname(file));
        }) || '';

        const id = crypto.createHash('md5').update(name).digest("hex");
        const hashedFolder = await hashElement(name, { files: { include: ['.mp3', '.ogg', '.jpg', '.png'] }});
        const durationInSeconds = getDuration(dirNames[name].filter(isAudioFile));

        const book = {
            id: crypto.createHash('md5').update(name).digest("hex"),
            title: "",
            author: "",
            path: name,
            hash: hashedFolder.hash,
            description: "",
            coverPath: coverLocalPath.replace('.' + path.sep, ''),
            filesCount: dirNames[name].filter(isAudioFile).length,
            durationInSeconds: durationInSeconds
        };

        books.push(book);
    }

    return books;
}

function isAudioFile(item) {
    return audioExts.includes(path.extname(item))
}

function walk(dir, filterExt = []) {
    let results = [];
    const list = fs.readdirSync(dir);

    list.forEach(function(file) {
        file = dir + path.sep + file;
        const stat = fs.lstatSync(file);
        if (stat && stat.isDirectory()) {
            results = results.concat(walk(file, filterExt));
        } else {
            const ext = path.extname(file);
            if (filterExt.length == 0 || filterExt.includes(ext)) {
                results.push(file);
            }
        }
    });

    return results;
}

function getDuration(files) {
    let duration = 0
// It takes too long on single thread
//    for(let i = 0; i < files.length; i+=1) {
//        const file = files[i];
//        const buffer = fs.readFileSync(file);
//        duration += getMP3Duration(buffer) / 1000;
//    }

    return Math.round(duration);
}

function getSavedBooks() {
    try {
        const indexFile = fs.readFileSync(indexFilePath);
        const index = JSON.parse(indexFile);

        return index;
    } catch(e) {
        return null;
    }
}

function calcBooksDiff(files, currentBooks) {
    if(null === currentBooks || (currentBooks.items || []).length === 0) {
        return { booksToAdd: files, booksToRemove: [] };
    }

    const booksIds = currentBooks.items.map((item) => {
        return item.id;
    });
    const filesIds = files.map(item => {
        return item.id;
    })

    const booksToAdd = filesIds.filter((id) => !booksIds.includes(id)).map((id) => {
        return files.find((file) => file.id === id);
    });
    const booksToRemove = booksIds.filter((id) => !filesIds.includes(id)).map((id) => {
        return currentBooks.items.find((book) => book.id === id);
    });

    return { booksToAdd, booksToRemove };
}

function removeFromIndex(books) {
    const savedBooks = getSavedBooks();
    const indexesToRemove = [];

    for(let i = 0; i < books.length; i+=1) {
        const book = books[i];
        const bookIndex = savedBooks.items.findIndex((savedBook) => savedBook.id === book.id);
        if(bookIndex > -1) indexesToRemove.push(bookIndex);
    }

    indexesToRemove.reverse().forEach((index) => {
        savedBooks.items.splice(index, 1);
    });

    module.exports.saveBooks(savedBooks.items);
}

function saveBooksFiles(books) {
    const paths = books.map((book) => '.' + path.sep + book.path).map((path) => walk(path, audioExts));
    paths.forEach((files) => {
        const model = {
            'metadata': {
                'total': files.length,
                'updatedAt': new Date().toISOString()
            },
            'items': files.map((item) => {
                return { 'path': item.replace('.' + path.sep, '')};
            })
        };
        const bookPath = files[0].split(path.sep, 2).join(path.sep) + path.sep + 'book.json';
        fs.writeFile(bookPath, JSON.stringify(model), 'utf8', (err) => {
            if (err) throw err;
        });
    });
}
