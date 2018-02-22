class CommentBox extends React.Component {
 render() {
    return <div>
      Hello {this.props.name}
    </div>
 }
}


ReactDOM.render(
  <CommentBox name="Taylor" />,
  document.getElementById('reactContainer')
);