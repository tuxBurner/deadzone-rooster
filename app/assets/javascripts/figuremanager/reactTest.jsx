class TroopTypeSelect extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      selectedFaction: undefined,
      troops: []
    }
  }

  setSelectedFaction(faction) {
    console.error(faction);
    this.setState({selectedFaction: faction});
  }

  getTroopsAndTypesFromBackend() {


    if(this.state.selectedFaction === undefined) {
      return;
    }

    jsRoutes.controllers.RosterController.getSelectTroopsForFaction(selectedFaction).ajax({
      success: function(data) {

        console.error(data);
      }.bind(this)
    });
  }

  render() {
    return <div class="input-group">
      <label>Faction:</label>
      <select class="form-control">
        
      </select>
      <p>{this.state.currentValue}</p>
    </div>
  }
}


class FactionSelect extends React.Component {


  constructor(props) {
    super(props);
    this.state = {
      factions: [],
      currentValue: null
    };

    this.handleSelectChange = this.handleSelectChange.bind(this);


  }
  

  /**
   * Call this when the componentn mounted to the dom
   */
  componentDidMount() {
    this.getFactionsFromBackend();
  }

  /**
   * Loads the factions from the backend
   */
  getFactionsFromBackend() {
    jsRoutes.controllers.RosterController.getFactions().ajax({
      success: function(data) {
        this.setState({factions: data,currentValue: data[0].name});
      }.bind(this)
    });
  }

  handleSelectChange(event) {
    this.setState({currentValue: event.target.value});
  }

  /**
   * This is called when the components state changed
   * @param prevProps
   * @param prevState
   */
  /*componentDidUpdate(prevProps, prevState) {
    console.error(this.state);
  } */




  render() {

    const troopTypeSelect = <TroopTypeSelect />;
    
    return <div class="input-group">
      <label>Faction:</label>
      <select class="form-control" onChange={this.handleSelectChange} value={this.state.currentValue}>
        {this.state.factions.map((faction) => <option key={faction.name} value={faction.name}>{faction.name}</option> )}
      </select>
      <p>{this.state.currentValue}</p>

      {troopTypeSelect}

    </div>


    
  }



}

/**
 * The main figure manager app
 */
class FigureManager extends React.Component {

  constructor(props) {
    super(props);
  }

  render() {
    return <div>
      <FactionSelect />
    </div>
  }


}


/**
 * Render the main app
 */
ReactDOM.render(
  <FigureManager/>,
  document.getElementById('reactContainer')
);