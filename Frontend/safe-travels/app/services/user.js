import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import User from 'safe-travels/models/user';

export default Ember.Service.extend({
  session: Ember.inject.service('session'),
  currentUser: null,
  getCurrentUser() {
    var service = this;
    if(this.get('currentUser')) {
      return this.get('currentUser');
    } else {
      var headers = {}
      console.log(this.session);
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      return Ember.$.ajax({
        url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
        headers: headers
      }).then(function(response) {
        var user = User.create();

        if(response.countries) {
          response.countries.forEach(function(country) {
            Ember.$.get(ENV.APP.apiUrl + '/country/' + country).then(function(country) {
              var userCountry = Ember.Object.create(country)
              userCountry.set('items', userCountry.get('items').map(function(i) {
                return Ember.Object.create(i);
              }));
              userCountry.set('vaccines', userCountry.get('vaccines').map(function(i) {
                return Ember.Object.create(i);
              }));

              userCountry.set('alerts', userCountry.get('alerts').map(function(i) {
                return Ember.Object.create(i);
              }));

              user.get('countries').pushObject(userCountry);
            });
          });
        }
        delete response.countries
        user.setProperties(response)
        service.set('currentUser', user)
        return service.get('currentUser');
      });
    }
  }
});
