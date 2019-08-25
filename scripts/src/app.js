const figlet = require('figlet');
const path = require('path');
const clear = require('clear');
const inquirer = require('inquirer');
const { questions } = require('./constants.js');
const db = require('./db.js');
const { findBookMeta, findBookCover } = require('./meta.js');

module.exports = {

    start: async () => {
        clear();
        out(figlet.textSync('BukPlayer'));

        const answer = await inquirer.prompt(questions.welcomeMenu);
        await processWelcomeMenu(answer.value);
    }
};

function out(message) {
    console.log(message);
}

async function processWelcomeMenu(answer) {
    const { SCAN_AND_UPDATE, MANUAL_UPDATE } = questions.welcomeMenu.answers;

    switch(answer) {
        case SCAN_AND_UPDATE:
            await scanAndUpdate();
            break;
        case MANUAL_UPDATE:
            manualUpdate();
            break;
        default:
            out('No such option.');
    }
}

async function scanAndUpdate() {
    out('Stats:');
    const savedBooks = db.readDb();
    out(' ' + savedBooks.metadata.total + ' book(s) in db');

    const booksOnDisk = await db.findBooks();
    out(' ' + Object.keys(booksOnDisk).length + ' book(s) on filesystem\n')

    const diff = db.diff(savedBooks, booksOnDisk);
    await removeBooks(diff.booksToRemove);
    await addBooks(diff.booksToAdd);

    out('Done.');
}

async function removeBooks(books) {
    const booksToRemove = books.length;
    if (booksToRemove == 0) return;

    out('Found ' + booksToRemove + ' book(s) to remove:');
    books.forEach(book => {
        out('- ' + book);
    });

    const answer = await inquirer.prompt(questions.continueQuestion);
    if(answer.value) {
        db.removeBooks(books);
        out('Books have been removed from db.');
    }
}

async function addBooks(books) {
    const size = books.length;
    if (size == 0) return;

    out('Found ' + size + ' book(s) to add.\n');

    for(let i = 0; i < size; i += 1) {
        const bookFile = books[i];
        const query = bookFile.path;

        out('[' + (i + 1) + '/' + size + '] Searching for "' + query + '"...');

        const meta = await findBookMeta(bookFile);
        const bookInfo = await askBookMeta(meta, bookFile, i + 1, size);
        bookInfo.filesDuration = getAudioFilesDuration(bookInfo);
        bookInfo.coverPath = await findBookCover(bookInfo);

        db.addBook(bookInfo);
        db.addBookFiles(bookInfo);
    }
}

async function askBookMeta(meta, bookFile, position, total) {
    const results = {};
    const properties = ['author', 'title', 'description', 'coverPath'];

    for(const key in properties) {
        const property = properties[key];
        clear();
        out('[' + position + '/' + total + '] "' + bookFile.path + '"');

        results[property] = (await questions.prompt(property, meta, inquirer)).value;
    }
    results['path'] = bookFile.path;
    results['files'] = bookFile.files;

    return results;
}

function getAudioFilesDuration(bookInfo) {
    return bookInfo.files.map((file) => bookInfo.path + path.sep + file).map((path, index, files) => {
        const progress = Math.round((index + 1) * 100 / files.length);
        clear();
        out('Reading mp3 files duration: ' + progress + '% done.');

        return { path: path, duration: db.getDuration(path) };
    });
}

function manualUpdate() {

}
