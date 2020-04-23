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
var TreeMapRender = (function (_super) {
    __extends(TreeMapRender, _super);
    function TreeMapRender() {
        var _this = _super !== null && _super.apply(this, arguments) || this;
        _this.config = {
            options: {
                "split-type": "squarifyV2",
                "color-type": "palette",
                palette: ["#f3a683", "#f7d794", "#778beb", "#f8a5c2", "#63cdda", "#009688"]
            },
            plotarea: {
                margin: "0 0 35 0",
                wordBreak: "break-all"
            },
            type: "treemap",
            series: []
        };
        return _this;
    }
    TreeMapRender.prototype.render = function (index, max) {
        var _this = this;
        max = max == null ? 20 : max;
        index = index == null ? Math.round(Math.random() * this.news.length) : index;
        var base = this.news[index];
        var distance = base.distance;
        var array;
        array = new Array();
        distance.forEach(function (value, index) {
            if (value >= 0 && value < 1) {
                array.push({ distance: value, index: index });
            }
        });
        var sorted = array.sort(function (a, b) {
            return b.distance - a.distance;
        });
        var childrenArray = [];
        sorted.forEach(function (value, index) {
            if (index < max) {
                var dis = Math.round(value.distance * 10000);
                var title = _this.news[value.index].title;
                childrenArray.push(TreeMapRender.buildChildren(title, value.distance * value.distance, _this.news[index].entities));
            }
        });
        var object = {
            'text': base.title,
            'children': childrenArray,
        };
        this.config['series'].push(object);
        return this.config;
    };
    TreeMapRender.buildChildren = function (text, value, children) {
        var _this = this;
        if (children != null) {
            var array_1 = [];
            children.forEach(function (v) {
                array_1.push(_this.buildChildren(v, v.length * v.length, null));
            });
            return { "text": text, "value": value, "children": array_1 };
        }
        else {
            return { "text": text, "value": value };
        }
    };
    return TreeMapRender;
}(Render));
//# sourceMappingURL=TreeMapRender.js.map