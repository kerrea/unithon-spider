var Render = (function () {
    function Render() {
        if (localStorage.getItem("page") == null) {
            var xhr_1 = new XMLHttpRequest();
            xhr_1.open("GET", "data.json", false);
            xhr_1.send();
            xhr_1.onreadystatechange = function () {
                if (xhr_1.readyState === 4) {
                    if (xhr_1.status == 200) {
                        localStorage.setItem("page", xhr_1.responseText);
                    }
                    else
                        alert("Error.");
                }
            };
        }
        this.news = JSON.parse(localStorage.getItem("page"));
    }
    return Render;
}());
//# sourceMappingURL=Render.js.map