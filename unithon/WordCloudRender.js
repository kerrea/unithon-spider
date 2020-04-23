var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var WordCloudRender = (function (_super) {
    __extends(WordCloudRender, _super);
    function WordCloudRender() {
        var _this = _super.call(this) || this;
        _this.config = {
            options: {
                words: []
            },
            plotarea: {
                margin: "0 0 35 0",
                wordBreak: "break-all"
            },
            type: "wordcloud"
        };
        _this.positions = [];
        if (localStorage.getItem("positions") == null) {
            var xhr_1 = new XMLHttpRequest();
            xhr_1.open("GET", "positions.json", false);
            xhr_1.send();
            xhr_1.onreadystatechange = function () {
                if (xhr_1.readyState === 4) {
                    if (xhr_1.status == 200) {
                        localStorage.setItem("positions", xhr_1.responseText);
                    }
                    else {
                        alert("Error.");
                    }
                }
            };
        }
        return _this;
    }
    WordCloudRender.prototype.render = function (index) {
        var _this = this;
        console.log(index);
        index = index == null ? "entities" : index;
        console.log(index);
        this.positions = JSON.parse(localStorage.getItem("positions"));
        var object = {};
        this.news.forEach(function (value) {
            value[index].forEach(function (entity) {
                if (object[entity] != null) {
                    object[entity]['count']++;
                }
                else {
                    object[entity] = WordCloudRender.build(entity, 1);
                }
            });
        });
        for (var entryKey in object) {
            this.config.options.words.push(object[entryKey]);
        }
        this.config.options['words'] = this.config.options.words.sort(function (i, j) {
            return j.count - i.count;
        }).filter(function (v) {
            if (index === "people") {
                return v.text.indexOf("卫") == -1
                    && v.text.indexOf("某") == -1
                    && v.text.indexOf("冠") == -1
                    && v.text.indexOf("楚天") == -1;
            }
            else {
                return _this.positions.indexOf(v.text) !== -1;
            }
        }).slice(0, 400);
        return this.config;
    };
    WordCloudRender.build = function (text, value) {
        return { "text": text, "count": value };
    };
    return WordCloudRender;
}(Render));
//# sourceMappingURL=WordCloudRender.js.map