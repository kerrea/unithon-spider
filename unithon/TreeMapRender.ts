class TreeMapRender extends Render {
    private config = {
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
    }

    public render(index, max) {
        max = max == null ? 20 : max;
        index = index == null ? Math.round(Math.random() * this.news.length) : index;
        let base = this.news[index];
        let distance = base.distance;
        let array: Array<{
            distance: number,
            index: number
        }>;
        array = new Array<{ distance: number, index: number }>();
        distance.forEach((value, index) => {
            if (value >= 0 && value < 1) {
                array.push({distance: value, index: index});
            }
        })
        let sorted = array.sort((a, b) => {
            return b.distance - a.distance
        });
        let childrenArray = [];
        sorted.forEach((value, index) => {
            if (index < max) {
                let dis = Math.round(value.distance * 10000);
                let title = this.news[value.index].title;
                childrenArray.push(TreeMapRender.buildChildren(title, value.distance * value.distance, this.news[index].entities));
            }
        });
        let object = {
            'text': base.title,
            'children': childrenArray,
        }
        this.config['series'].push(object);
        return this.config;
    }

    private static buildChildren(text, value, children) {
        if (children != null) {
            let array = [];
            children.forEach((v) => {
                array.push(this.buildChildren(v, v.length * v.length, null));
            });
            return {"text": text, "value": value, "children": array};
        } else {
            return {"text": text, "value": value};
        }
    }
}