import ENV from 'safe-travels/config/environment';

export default Ember.Route.extend({
  model(params) {
    return Ember.$.get(ENV.APP.apiUrl + '/register/' + params.uuid);
  }
})
