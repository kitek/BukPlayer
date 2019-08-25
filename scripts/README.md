# Database builder 

### The idea

Database builder is a basic script to help you create all required files by BukPlayer. Instead of manually updating metafiles each time you add or remove an audiobook you can simply run the builder script. 

Features:
- suggest corrected author name and audiobook title
- download covers and audiobook descriptions
- in case of wrong suggestions allows manual edition
- calculate audiobook files duration

![Database builder](https://github.com/kitek/BukPlayer/raw/master/scripts/assets/db-script-1.png "Database builder")

![Choose author name](https://github.com/kitek/BukPlayer/raw/master/scripts/assets/db-script-2.png "Choose author name")

### Files structure

The main database file is called "index.json" and should be placed in the root directory accessible from URL provided in BukPlayer settings.

Example index.json file:

```
{
    "metadata": {
        "total": 1,
        "updatedAt": "2019-08-25T08:09:56.938Z"
    },
    "items": [
        {
            "id": "774e0fcd1a11e524c0a066f5f39b167d",
            "title": "Metro 2033",
            "author": "Dmitry Glukhovsky",
            "path": "Dmitry Glukhovsky - Metro 2033",
            "coverPath": "Dmitry Glukhovsky - Metro 2033/Dmitry Glukhovsky - Metro 2033.jpg",
            "description": "Odświeżone, drugie wydanie kultowej powieści Glukhovsky’ego...",
            "filesCount": 20,
            "durationInSeconds": 66310
        }
    ]   
}
```

Each directory represents one audiobook, the directory name is used to find metadata so recommended is name it with "author name - book title"

Example directory tree:
```
/
    - "Dmitry Glukhovsky - Metro 2033"
        - 01.Metro 2033-Roz.I.mp3
        - 02.Metro 2033-Roz.II.mp3
        - cover.jpg
        - book.json

    - "Brown Dan - Inferno"
        - Inferno_001_Prolog.mp3
        - Inferno_002_Rozdzial_001.mp3
        - cover.jpg
        - book.json
```

Details about audio files are placed inside "book.json" inside every audiobook directory.

Example book.json file:

```
{
    "metadata": {
        "total": 2,
        "updatedAt": "2019-08-25T08:09:56.938Z"
    },
    "items": [
        {
            "path": "Dmitry Glukhovsky - Metro 2033/Inferno_001_Prolog.mp3",
            "duration": 3156 
        },
        {
            "path": "Dmitry Glukhovsky - Metro 2033/Inferno_002_Rozdzial_001.mp3",
            "duration": 3028 
        }
    ]
}
```

### Metadata scrappers

Scrappers are placed in src/scrapers directory and should follow simple interface:

```
module.exports = (query, limit = 2) => {
    // scrap information here
    
    return [
        { 
            title: "Some scrapped title", 
            author: "Scrapped author name",
            description: "Awesome audiobook description",
            coverPath: "https://some-cover/cover.jpg"  
        }
    ];
}
```

### Requirements and limitations

- Node.js v12.7.0
- Flat audiobook directories structure
- Supported audio files: mp3, ogg
- Supported cover files: png, jpg 

