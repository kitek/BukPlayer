const COVER_EXT = ['.png', '.jpg'];
const AUDIO_EXT = ['.mp3', '.ogg'];
const WELCOME_ANSWERS = {
    SCAN_AND_UPDATE: 1,
    MANUAL_UPDATE: 2
};
const PROMPT_ANSWERS = {
    TYPE_MANUALLY: 1
};

module.exports = {

    coverExtensions: COVER_EXT,
    audioExtensions: AUDIO_EXT,
    supportedExtensions: COVER_EXT.concat(AUDIO_EXT, '.json'),

    questions: {

        prompt: function(property, proposals, inquirer) {
            const options = removeDuplicates(property, proposals);
            const manualOption = {
                name: 'Type ' + property + ' manually',
                value: PROMPT_ANSWERS.TYPE_MANUALLY
            };
            const manualQuestion = {
                type: 'input',
                name: 'value',
                message: 'Enter ' + property + ':'
           };

            if (options.length === 0) {
                return inquirer.prompt(manualQuestion);
            }

            return inquirer.prompt({
                type: 'list',
                name: 'value',
                message: 'Choose or type ' + property,
                choices: options.concat(manualOption)
            }).then(async (answer) => {
                if(answer.value !== PROMPT_ANSWERS.TYPE_MANUALLY) return answer;
                return await inquirer.prompt(manualQuestion);
            });
        },

        welcomeMenu : {
            answers: WELCOME_ANSWERS,
            type: 'list',
            name: 'value',
            message: 'Menu',
            choices: [
              {
                  name: 'Scan & update',
                  value: WELCOME_ANSWERS.SCAN_AND_UPDATE
              },
//              {
//                  name: 'Manual update db',
//                  value: WELCOME_ANSWERS.MANUAL_UPDATE
//              }
            ]
        },
        continueQuestion: {
            type: 'confirm',
            name: 'value',
            message: 'Would you like to continue?'
        },
    }
};

function removeDuplicates(property, proposals) {
    return proposals.map((item) => {
        return (item[property] || '').trim().replace("\n", " ");
    }).filter((item) => {
        return item.length > 0;
    }).reduce((unique, item) => {
        return unique.includes(item) ? unique : [...unique, item]
    }, []);
}
