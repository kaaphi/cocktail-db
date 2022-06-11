import logging
from typing import Optional

from jinja2 import Environment, PackageLoader, select_autoescape

env = Environment(
    loader=PackageLoader("cocktail_db"),
    autoescape=select_autoescape()
)

logger = logging.getLogger()
logger.setLevel(logging.INFO)


class Route:
    def __init__(self, template: str, model_supplier) -> None:
        self.template = template
        self.model_supplier = model_supplier


def handle(event: dict) -> Optional[str]:
    routes = {
        '/': Route("index.html", lambda e: {}),
        '/alphabetical': Route("recipe_list.html", alphabetical_list)
    }

    path = event['rawPath']
    logger.info('Path: %s', path)
    route = routes.get(path)

    if not route:
        raise Exception(f'Unknown route: {path}')

    template = env.get_template(route.template)
    model = route.model_supplier(event)

    return template.render(model)


def alphabetical_list(event):
    return {
        'title' : 'Alphabetical',
        'recipes': [{
            'uri_title': 'one',
            'name': 'One',
        }]
    }



