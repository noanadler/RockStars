import Ember from 'ember';

export default Ember.Service.extend({
  countries: null,
  countryNames: Ember.computed('countries[]', function() {
    return this.get('countries').mapBy('text');
  }),
  init() {
    this.set('countries', []);
  },
});
