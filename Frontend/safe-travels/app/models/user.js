import Ember from 'ember';

export default Ember.Object.extend({
  email: null,
  gender: null,
  birthDate: null,
  countries: [],
  countryNames: Ember.computed('countries.[]', function() {
    console.log('in here');
    return this.get('countries').mapBy('text');
  })
});
