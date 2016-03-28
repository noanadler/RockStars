import Ember from 'ember';
import ENV from 'safe-travels/config/environment';
import User from 'safe-travels/models/user';
import AuthenticatedRouteMixin from 'ember-simple-auth/mixins/authenticated-route-mixin';

export default Ember.Route.extend(AuthenticatedRouteMixin, {
  session: Ember.inject.service('session'),
  model() {
    var headers = {}
    this.get('session').authorize('authorizer:token', (header, token) => {
      headers[header] = token;
    });

    return Ember.RSVP.hash({
      countries: Ember.$.get(ENV.APP.apiUrl + '/countries').then(function(response) {
        return response.map(function(country) {
          return Ember.Object.create({ id: country.country, text: country.full_name});
        });
      }),
      user: Ember.$.ajax({
        url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
        headers: headers
      }).then(function(response) {
        var user = User.create();
        if(response.countries) {
          response.countries.forEach(function(country) {
            Ember.$.get(ENV.APP.apiUrl + '/country/' + country).then(function(country) {
              var country = Ember.Object.create(country)
              country.set('items', country.get('items').map(function(i) {
                return Ember.Object.create(i);
              }));
              country.set('vaccines', country.get('vaccines').map(function(i) {
                return Ember.Object.create(i);
              }));

              user.get('countries').pushObject(Ember.Object.create(country));
            });
          });
        }
        delete response.countries
        user.setProperties(response)
        return user;
      })
    })
  },
  actions: {
    submit() {
      var headers = {}
      this.get('session').authorize('authorizer:token', (header, token) => {
        headers[header] = token;
      });

      //console.log(this.get('controller.model.user').toJSON());
      this.get('controller.model.user').set('countries', this.get('controller.countries').map(function(c) {
        return { country: c.id };
      }));

      Ember.$.ajax({
        url: ENV.APP.apiUrl + '/users/' + this.get('session.data.user'),
        method: 'PUT',
        headers: headers,
        contentType: "application/json; charset=utf-8",
        dataType:'json',
        data: this.get('controller.model.user').toJSON()
      })

      this.transitionTo('dashboard')
    }
  }
});
