import Ember from 'ember';
import ENV from 'safe-travels/config/environment';

export default Ember.Route.extend({
  trip: Ember.inject.service('trip'),
  model() {
    return Ember.RSVP.hash({
      countries: this.get('trip.countries').map(function(trip) {
        Ember.$.get(ENV.APP.apiUrl + '/country/' + trip.get('id'));
      }),
      trip: this.get('trip')
    })
  }
});
