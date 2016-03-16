sig Name {}
sig Date {}
sig BirthdayBook {known: set Name, date: known -> one Date}

run {}
