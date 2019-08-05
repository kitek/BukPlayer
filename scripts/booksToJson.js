const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const https = require('https');
const readline = require('readline');
const { URL } = require('url');

function walk(dir, filterExt = []) {
    let results = [];
    const list = fs.readdirSync(dir);

    list.forEach(function(file) {
        file = dir + '/' + file;
        const stat = fs.statSync(file);
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

function fetchJson(url, callback) {
    const myURL = new URL(url);
    const options = {
        hostname: myURL.hostname,
        port: 443,
        path: myURL.pathname+myURL.search,
        method: 'GET'
    };
    const req = https.request(options, (res) => {
        let body = '';
        res.on('data', (d) => {
            body += d;
        });

        res.on('end',() => {
            try {
                callback(JSON.parse(body))
            } catch(e) {
                console.log('ERROR', e);
                callback(null, e);
            }
        });
    });

    req.on('error', (e) => {
        callback(null, e);
    });
    req.end();
}

function isMp3(item) {
    return path.extname(item) === '.mp3'
}

function mapToBooks(files) {
    const dirNames = {};

    files.forEach(function(file) {
        const firstDirName = file.split(path.sep)[1];

        if (!dirNames.hasOwnProperty(firstDirName)) {
            dirNames[firstDirName] = [];
        }
        dirNames[firstDirName].push(file);
    });

    const books = Object.keys(dirNames).map((name) => {
        const coverLocalPath = dirNames[name].find((file) => {
            return ['.jpg', '.png'].includes(path.extname(file));
        }) || '';

        return {
            id: crypto.createHash('md5').update(name).digest("hex"),
            title: name,
            author: "",
            path: name,
            hash: "",
            description: "",
            coverPath: coverLocalPath.replace('./',''),
            filesCount: dirNames[name].filter(isMp3).length,
            durationInSeconds: 0
        }
    })

    return {books, dirNames};
}

function question(question, callback) {
    const rl = readline.createInterface({
        input: process.stdin,
        output: process.stdout
    });

    rl.question(question, (answer) => {
        rl.close();
        callback(answer);
    });
}

function saveFilesToJson(dirNames) {
    Object.keys(dirNames).map((key) => {
        const files = dirNames[key].filter(isMp3);
        const model = {
            'metadata': {
                'total': files.length,
                'updatedAt': new Date().toISOString()
            },
            'items': files.map((item) => {
                return { 'path': item.replace('./', '')};
            })
        };
        const bookPath = dirNames[key][0].split(path.sep, 2).join(path.sep) + path.sep + 'book.json';

        fs.writeFile(bookPath, JSON.stringify(model), 'utf8', (err) => {
            if (err) throw err;
        });
    });
}

function findMeta(books, index = 0) {
    if (books.length < index + 1) {
        const filePath = './index.json';
        const model = {
            'metadata': {
                'total': books.length,
                'updatedAt': new Date().toISOString()
            },
            'items': books
        };
        fs.writeFile(filePath, JSON.stringify(model), 'utf8', (err) => {
            if (err) throw err;
        });
        console.log('Done.');

        return;
    }

    const bookName = books[index].title;
    const url = 'https://www.googleapis.com/books/v1/volumes?q='+bookName;
    console.log('\n[' + (index+1) + '/' + books.length + '] Looking for "' + bookName+'"...');

    fetchJson('https://www.googleapis.com/books/v1/volumes?q='+bookName, (result, error) => {

        if(!result) {
            console.log('Nothing found :(\n');

            return findMeta(books, index + 1);
        }

        const titleProposal = result.items[0].volumeInfo.title || '';
        const authorProposal = result.items[0].volumeInfo.authors[0] || '';
        const descriptionProposal = result.items[0].volumeInfo.description || '';
        console.log('Found: ' + titleProposal + ' | ' + authorProposal);

        question('\nAre title and author correct? [y/n]: ', (answer) => {
            if ('y' === answer) {
                books[index].title = titleProposal;
                books[index].author = authorProposal;
                books[index].description = descriptionProposal;
            }

            return findMeta(books, index + 1);
        });
    });
}

const files = walk('.', ['.mp3', '.jpg', '.png']);
const {books, dirNames} = mapToBooks(files);
saveFilesToJson(dirNames);
findMeta(books);
