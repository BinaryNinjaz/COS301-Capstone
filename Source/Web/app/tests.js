QUnit.test("Password Matching", function ( assert ) {
    assert.ok(checkPass("123", "123"));
    assert.ok(checkPass("Hello World", "Hello World"));
    assert.notOk(checkPass("hello world", "Hello World"));
    assert.notOk(checkPass("123", ""));
    assert.notOk(checkPass("", ""));
    assert.notOk(checkPass("", "123"));
});