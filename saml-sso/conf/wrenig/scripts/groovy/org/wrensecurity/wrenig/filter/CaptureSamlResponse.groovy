package org.wrensecurity.wrenig.filter

// Preserve AM's posted SAML response for the cookbook's /assertion endpoint
def encodedResponse = request.form['SAMLResponse']?.first()
if (encodedResponse) {
    session.remove('username')
    session.samlResponse = new String(encodedResponse.decodeBase64(), 'UTF-8')
}

return next.handle(context, request)
