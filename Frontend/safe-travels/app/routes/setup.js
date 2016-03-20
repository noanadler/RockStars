import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Route.extend({
  trip: Ember.inject.service('trip'),
  model() {
    return Ember.RSVP.hash({
      countries: Ember.$.get(ENV.APP.apiUrl + '/countries').then(function(response) {
        return response.map(function(country) {
          return Ember.Object.create({ id: country.country, text: country.full_name});
        });
      }),
      trip: this.get('trip')
    })
  },
  actions: {
    submit() {
      this.transitionTo('dashboard')
    }
  }
});
