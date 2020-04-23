class WordCloudRender extends Render {
    private config = {
        options: {
            words: []
        },
        plotarea: {
            margin: "0 0 35 0",
            wordBreak: "break-all"
        },
        type: "wordcloud"
    }

    private positions = [];
    constructor() {
        super();
        if (localStorage.getItem("positions") == null) {
            let xhr = new XMLHttpRequest();
            xhr.open("GET", "positions.json",false);
            xhr.send();
            xhr.onreadystatechange = () => {
                if (xhr.readyState === 4) {
                    if (xhr.status == 200) {
                        localStorage.setItem("positions", xhr.responseText);
                    } else {
                        alert("Error.");
                    }
                }
            }
        }
    }

    render(index) {
        console.log(index);
        index = index == null ? "entities" : index;
        console.log(index);
        this.positions = JSON.parse(localStorage.getItem("positions"));
        let object = {};
        this.news.forEach((value) => {
            value[index].forEach((entity) => {
                if (object[entity] != null) {
                    object[entity]['count']++;
                } else {
                    object[entity] = WordCloudRender.build(entity, 1);
                }
            });
        });
        for (let entryKey in object) {
            this.config.options.words.push(object[entryKey]);
        }
        this.config.options['words'] = this.config.options.words.sort((i, j) => {
            return j.count - i.count;
        }).filter((v) => {
            if(index === "people"){
                return v.text.indexOf("卫") == -1
                    && v.text.indexOf("某") == -1
                    && v.text.indexOf("冠") == -1
                    && v.text.indexOf("楚天") == -1;
            }else {
                return this.positions.indexOf(v.text) !== -1;
            }
        }).slice(0, 400);
        return this.config;
    }

    private static build(text, value) {
        return {"text": text, "count": value};
    }
}