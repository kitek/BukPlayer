const path = require('path');
const figlet = require('figlet');
const clear = require('clear');
const { findBooks, addBooks, removeBooks } = require('./dirUtils.js');
const { findMeta } = require('./meta.js');

async function main() {
    clear();
    console.log(figlet.textSync('BukPlayer'));

    const { booksToAdd, booksToRemove } = await findBooks();
    removeBooks(booksToRemove);

    const booksWithMeta = await findMeta(booksToAdd);
    addBooks(booksWithMeta);

    console.log('Done.')
}

main();
