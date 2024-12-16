from http.server import BaseHTTPRequestHandler
from clip_cpp import Clip
from urllib.parse import urlparse, parse_qs
import json

clip = Clip(
    model_path_or_repo_id="api/CLIP-ViT-B-32-laion2B-s34B-b79K_ggml-text-model-q4_0.gguf",
    verbosity=0
)

class handler(BaseHTTPRequestHandler):

    def do_GET(self):
        query_components = parse_qs(urlparse(self.path).query)
        query = query_components.get('query', [''])[0]
        
        if not query:
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b"Error: 'query' query parameter is required")
            return
        
        tokens = clip.tokenize(query)        
        embedding = clip.encode_text(tokens)

        response = {
            "embedding": embedding
        }

        self.send_response(200)
        self.send_header('Content-type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(response).encode())

        return