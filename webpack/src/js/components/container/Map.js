import React, { Component } from 'react';
import ReactDOM from 'react-dom';


class Map extends React.Component {
    constructor(props) {
        super(props);
        this.state = {
            info: false,
            view: false,
            data: false
        };
        this.state.info = props.info;
        this.state.view = props.view;
    }

    render() {
        if (this.state.minX || this.state.maxX) {

            var rows = [];

            for (let y = this.state.minY; y <= this.state.maxY; y++) {
                var row = [];
                for (let x = this.state.minX; x <= this.state.maxX; x++) {
                    let i = `/api/sector/${x},${y}/image`;
                    row.push(<td><img src={i} width="128" height="160"/></td>);
                }
                rows.push(<tr>{row}</tr>);
            }
            return (
                <table className="galaxy_map">
                    {rows}
                </table>
            )

        } else {
            return false;
        }
    }
}

export default GalacticMap;
