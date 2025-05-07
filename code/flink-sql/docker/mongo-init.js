db = db.getSiblingDB('mydb');

db.createCollection('user_data');

db.user_data.insertMany([
  { fname: "John", lname: "Doe", email: "john@example.com" },
  { fname: "Alice", lname: "Smith", email: "alice@example.com" },
  { fname: "Bob", lname: "Johnson", email: "bob@example.com" },
  { fname: "Charlie", lname: "Ray", email: "" },  // invalid email
  { fname: "Eva", lname: "Green", email: null }   // invalid email
]);
