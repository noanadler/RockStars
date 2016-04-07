import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import User from 'safe-travels/models/user';

export default Ember.Service.extend({
  session: Ember.inject.service('session'),
  currentUser: null,
  loadUser() {
    var service = this;

    console.log('loading user');

    var headers = {}
    this.get('session').authorize('authorizer:token', (header, token) => {
      headers[header] = token;
    });

    return Ember.$.ajax({
      url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
      headers: headers
    }).then(function(response) {
      var user = User.create();
      user.set('countries', []);
      user.set('completedVaccines', response.vaccines)

      if(response.countries) {
        response.countries.forEach(function(country) {
          Ember.$.get(ENV.APP.apiUrl + '/country/' + country).then(function(country) {
            user.addCountry(country);
          });
        });
      }
      delete response.countries
      delete response.vaccines
      user.setProperties(response)
      service.set('currentUser', user)
      return service.get('currentUser');
    });
  },
  getCurrentUser() {
    var service = this;
    if(this.get('currentUser')) {
      console.log('returning user');

      return this.get('currentUser');
    } else {
      return this.loadUser()
    }
  }
});
