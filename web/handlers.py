import json

import cocktail_db.ui


def ui_handler(event, context):
    body = cocktail_db.ui.handle(event)

    if body:
        response = {
            "statusCode": 200,
            "body": body,
            "headers": {
                "content-type": "text/html"
            }
        }
    else:
        response = {
            "statusCode": 404
        }

    return response

    # Use this code if you don't use the http event with the LAMBDA-PROXY
    # integration
    """
    return {
        "message": "Go Serverless v1.0! Your function executed successfully!",
        "event": event
    }
    """
