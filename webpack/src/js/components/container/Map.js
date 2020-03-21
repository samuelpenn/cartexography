import React, { Component } from 'react';
import ReactDOM from 'react-dom';


class Map extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            info: false,
            view: false,
            data: false,
            terrain: false
        };
        this.state.info = this.props.info;
        this.state.view = this.props.view;
    }

    componentDidMount() {
        var _self = this;

        fetch("/api/map/" + this.state.info.name + "/data", { headers: { 'Content-Type': 'application.json'}})
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                _self.setState({ data: data });
            })
            .catch(function(error) {
                console.log(error);
            })
        fetch("/api/map/" + this.state.info.name + "/terrain", { headers: { 'Content-Type': 'application.json'}})
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                // Now load all the images.

                console.log("Loading " + data.length + " terrain images...");
                var imagesToLoad = data.length;
                for (let i=0; i < data.length; i++) {
                    let t = data[i];
                    t.image = new Image();
                    t.onLoad = function (e) {
                        imagesToLoad -= 1;
                        console.log("Images to load: " + imagesToLoad);
                        if (imagesToLoad < 1) {
                            _self.setState({ terrain: data });
                        }

                    };
                    t.image.src = "/images/style/standard/terrain/" + t.name + "_0.png";
                    console.log("Load [" + t.name + "]");
                }

                // Finally, set the state to say that we are ready to draw things.
                _self.setState({ terrain: data });
            })
            .catch(function(error) {
                console.log(error);
            })
    }

    componentDidUpdate() {
        if (this.state.data && this.state.terrain) {
            let hexData = this.state.data.terrain;

            const canvas = this.refs.canvas;
            const ctx = canvas.getContext("2d");
            ctx.setTransform(1, 0, 0, 1, 0, 0);
            ctx.scale(0.25, 0.25);

            let zoom = [ { column: 48, row: 56, width: 65, height: 56, font: 14, step: 10, scale: 1 },
                { column: 24, row: 28, width: 33, height: 28, font: 12, step: 10, scale: 1 },
                { column: 12, row: 14, width: 17, height: 14, font: 9,  step: 20, scale: 1 },
                { column: 6, row: 7, width: 9, height: 7, font: 6, step: 40, scale: 1  },
                { column: 8, row: 8, width: 8, height: 8, font: 0, step: 400, scale: 10 },
                { column: 4, row: 4, width: 4, height: 4, font: 0, step: 1600, scale: 20 } ];

            let scale = 64;

            let columnWidth = scale;
            let rowHeight = (columnWidth * 6.0) / 5.0;
            let imageWidth = (columnWidth * 4.0) / 3.0;

            for (let z = 0; z < 2; z++) { // HACK: Until we can get around the onLoad() bug for images.
                for (let y = 0; y < hexData.length; y++) {
                    let row = hexData[y];
                    for (let x = 0; x < hexData[y].length; x++) {
                        let tId = row[x] / 100;

                        let terrain = null;
                        for (let t = 0; t < this.state.terrain.length; t++) {
                            if (this.state.terrain[t].id == tId) {
                                terrain = this.state.terrain[t];
                                ctx.fillStyle = this.state.terrain[t].colour;
                                break;
                            }
                        }
                        let px = x * columnWidth;
                        let py = y * rowHeight + (x%2 * rowHeight / 2.0);

                        //ctx.fillRect(px, py, imageWidth, imageWidth);
                        if (terrain != null) {
                            ctx.drawImage(terrain.image, px, py, imageWidth, imageWidth);
                        }
                    }
                }
            }
        }
    }

    render() {
        if (this.state.data) {
            return (
                <canvas ref="canvas" />
            )
        } else {
            return (
                <canvas ref="canvas" />
            )
        }
    }
}

export default Map;
