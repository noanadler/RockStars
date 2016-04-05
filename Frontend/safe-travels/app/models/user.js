import Ember from 'ember';

var User = Ember.Object.extend({
  session: Ember.inject.service('session'),
  email: null,
  gender: null,
  birthDate: null,
  notification: false,
  verified: false,
  password: null,
  name: null,
  countries: [],
  alerts: [],
  countryNames: Ember.computed('countries.[]', function() {
    return this.get('countries').mapBy('full_name');
  }),
  packingListItems: Ember.computed('countries.[]', function() {
    var packingListItems = [];
    this.get('countries').forEach(function(c) {
      //c.packing
      packingListItems.pushObjects(c.get('items'));
    });

    return this.uniqByName(packingListItems);
  }),
  vaccines: Ember.computed('countries.[]', function() {
    var vaccines = [];
    this.get('countries').forEach(function(c) {
      //c.packing
      vaccines.pushObjects(c.get('vaccines'));
    });

    return this.uniqByName(vaccines);
  }),
  alerts: Ember.computed('countries.[]', function() {
    var alerts = [];
    this.get('countries').forEach(function(c) {
      //c.packing
      alerts.pushObjects(c.get('alerts'));
    });

    return this.uniqByName(alerts);
  }),
  uniqByName: function(items) {
    var names = [];
    var uniqItems = []

    items.forEach(function(item) {
        if(!names.contains(item.get('name'))) {
          names.pushObject(item.get('name'));
          uniqItems.pushObject(item);
        }
    });

    return uniqItems;
  },
  toJSON: function() {
    var json = {
      email: this.get('email'),
      gender: this.get('gender'),
      notification: this.get('notification'),
      countries: this.get('countries').mapBy('country'),
      name: this.get('name')
    }

    if(this.get('password')) {
      json.password = this.get('password');
    }

    return JSON.stringify(json);
  },
  asJSON: function() {
    var json = {
      email: this.get('email'),
      gender: this.get('gender'),
      notification: this.get('notification'),
      name: this.get('name')
    }

    if(this.get('password')) {
      json.password = this.get('password');
    }

    return json
  },
  addCountry(country) {
    if(!this.get('countries').mapBy('country').contains(country.country)) {
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

      this.get('countries').pushObject(userCountry);
    }
  }
});

export default User;
