# Copyright (c) Meta Platforms, Inc. and affiliates.
# This software may be used and distributed according to the terms of the Llama 2 Community License Agreement.

from typing import List, Optional
from flask import Flask, jsonify, request
import fire
from llama import Llama, Dialog

app = Flask(__name__)

class LlamaStorage:

    def __init__(self):

        self.user_id = None
        self.dialogs: List[Dialog] = []
        self.dialog = []
        self.generator = None

        self.temprature = 0
        self.top_p = 0
        self.max_get_len = 0


    def prompt_injection(self):
        # Prompt injection
        self.dialog.append({
            "role": "system",
            "content": "Be energetic. Use emojis. Do short answers. ",
        })

        #pass

    def setup(self, ckpt_dir, tokenizer_path, temperature, top_p, max_seq_len, max_batch_size, max_gen_len):

        print("Initializing: ")

        self.temprature = temperature
        self.top_p = top_p
        self.max_get_len = max_gen_len

        self.prompt_injection()

        print("Ready to load generator" + "\n")

        self.generator = Llama.build(
            ckpt_dir=ckpt_dir,
            tokenizer_path=tokenizer_path,
            max_seq_len=max_seq_len,
            max_batch_size=max_batch_size,
        )

        print("Generator loaded. \n")

storage = LlamaStorage()

@app.route('/process_chat', methods=['POST'])
def process_chat():
    try:
        # Get the JSON data from the request body
        data = request.get_json()

        # Check if the 'id' key exists in the JSON data
        if 'user_id' in data and 'content' in data:

            if data['user_id'] != storage.user_id:
                storage.dialog = []
                storage.prompt_injection()
            
            storage.user_id = data['user_id']  # Store the 'id' in the class attribute

            storage.dialog.append({"role": "user", "content": f"{data['content']}"})
            storage.dialogs = [storage.dialog]

            print("Dialogs == " + str(storage.dialogs) + "\n")

            results = storage.generator.chat_completion(
                storage.dialogs,  # type: ignore
                max_gen_len=storage.max_get_len,
                temperature=storage.temprature,
                top_p=storage.top_p,
            )

            for dialog, result in zip(storage.dialogs, results):
                for msg in dialog:
                    print(f"{msg['role'].capitalize()}: {msg['content']}\n")
                
                print(
                    f"> {result['generation']['role'].capitalize()}: {result['generation']['content']}"
                )
                print("\n==================================\n")

                dialog.append({"role": "assistant", "content": f"""\{result['generation']['content']}"""})
                response_data = {'content': f"""\{result['generation']['content']}"""}
            
            return jsonify(response_data), 200
        else:
            return jsonify(error='Missing "id" in request body'), 400
        
    except Exception as e:
        return jsonify(error=str(e)), 500  


def main(
    ckpt_dir: str,
    tokenizer_path: str,
    temperature: float = 0.6,
    top_p: float = 0.9,
    max_seq_len: int = 512,
    max_batch_size: int = 8,
    max_gen_len: Optional[int] = None,
):
    """
    Entry point of the program for generating text using a pretrained model.

    Args:
        ckpt_dir (str): The directory containing checkpoint files for the pretrained model.
        tokenizer_path (str): The path to the tokenizer model used for text encoding/decoding.
        temperature (float, optional): The temperature value for controlling randomness in generation.
            Defaults to 0.6.
        top_p (float, optional): The top-p sampling parameter for controlling diversity in generation.
            Defaults to 0.9.
        max_seq_len (int, optional): The maximum sequence length for input prompts. Defaults to 512.
        max_batch_size (int, optional): The maximum batch size for generating sequences. Defaults to 8.
        max_gen_len (int, optional): The maximum length of generated sequences. If None, it will be
            set to the model's max sequence length. Defaults to None.
    """
    storage.setup(ckpt_dir, tokenizer_path, temperature, top_p, max_seq_len, max_batch_size, max_gen_len)
    



if __name__ == "__main__":
    print("Script loaded! \n")
    fire.Fire(main)
    app.run(debug=False, host='0.0.0.0', port=5000)