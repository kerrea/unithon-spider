abstract class Render {
    protected readonly news: Array<{
        "distance": Array<number>,
        "entities": Array<string>,
        "organizations": Array<string>,
        "positions": Array<string>,
        "people": Array<string>,
        "title": string
        "content": string
    }>;

    public constructor() {
        if (localStorage.getItem("page") == null) {
            let xhr = new XMLHttpRequest();
            xhr.open("GET", "data.json",false);
            xhr.send();
            xhr.onreadystatechange = () => {
                if (xhr.readyState === 4) {
                    if (xhr.status == 200) {
                        localStorage.setItem("page", xhr.responseText);
                    } else alert("Error.");
                }
            }
        }
        this.news = JSON.parse(localStorage.getItem("page"));
    }

    public abstract render(index?, max?);
}