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

                console.log("Loading " + data.length + " terrain images.");
                let imagesToLoad = 0;
                for (let i=0; i < data.length; i++) {
                    let t = data[i];
                    t.image = new Image();
                    t.loaded = false;
                    t.onload = function() { t.loaded = true; };
                    t.image.src = "/images/style/standard/terrain/" + t.name + "_0.png";
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
            ctx.scale(4, 4);

            let scale = 2;
            for (let y=0; y < hexData.length; y++) {
                let row = hexData[y];
                for (let x=0; x < hexData[y].length; x++) {
                    let tId = row[x] / 100;

                    let terrain = null;
                    for (let t=0; t < this.state.terrain.length; t++) {
                        if (this.state.terrain[t].id == tId) {
                            terrain = this.state.terrain[t];
                            ctx.fillStyle = this.state.terrain[t].colour;
                            break;
                        }
                    }

                    ctx.fillRect(x * scale, y * scale, scale, scale);
                    if (terrain != null) {
                        ctx.drawImage(terrain.image, x * scale, y * scale, scale, scale);
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
