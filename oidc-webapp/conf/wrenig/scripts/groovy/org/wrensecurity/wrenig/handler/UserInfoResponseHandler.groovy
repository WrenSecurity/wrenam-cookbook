package org.wrensecurity.wrenig.handler

import org.forgerock.http.protocol.Response
import org.forgerock.http.protocol.Status

// OAuth2ClientFilter obtains UserInfo from AM and stores it in this attribute
def response = new Response(Status.OK)
response.setEntity(attributes.openid.user_info)
return response
