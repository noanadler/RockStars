import Ember from 'ember';

export default Ember.Service.extend({
  countries: null,
  name: "TEST",
  init() {
    this.set('countries', []);
  },
});
