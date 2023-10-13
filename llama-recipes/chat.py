from typing import List, Optional
from flask import Flask, jsonify, request
import fire
from llama import Llama, Dialog


class LlamaStorage:

    def __init__(self):

        self.user_id = None
        self.dialogs: List[Dialog] = []
        self.dialog = []
        self.generator = None

        # Prompt injection
        self.dialog.append({
            "role": "system",
            "content": "Give rude answers",
        })

    def setup(self, ckpt_dir, tokenizer_path, max_seq_len, max_batch_size):

        print("Initializing: ")
        print("Ready to load generator" + "\n")

        self.generator = Llama.build(
            ckpt_dir=ckpt_dir,
            tokenizer_path=tokenizer_path,
            max_seq_len=max_seq_len,
            max_batch_size=max_batch_size,
        )

        print("Generator loaded. \n")

    


def main(
    ckpt_dir: str,
    tokenizer_path: str,
    temperature: float = 0.6,
    top_p: float = 0.9,
    max_seq_len: int = 512,
    max_batch_size: int = 3,
    max_gen_len: Optional[int] = 40,
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

    storage = LlamaStorage()

    storage.setup(ckpt_dir, tokenizer_path, max_seq_len, max_batch_size)

    while True:

        user_input = input("You: ")

        storage.dialog.append({"role": "user", "content": f"{user_input}"})
        storage.dialogs = [storage.dialog]

        print("Dialogs == " + str(storage.dialogs) + "\n")

        results = storage.generator.chat_completion(
            storage.dialogs,  # type: ignore
            max_gen_len=max_gen_len,
            temperature=temperature,
            top_p=top_p,
        )

        for dialog, result in zip(storage.dialogs, results):
            for msg in dialog:
                print(f"{msg['role'].capitalize()}: {msg['content']}\n")
            
            print(
                f"> {result['generation']['role'].capitalize()}: {result['generation']['content']}"
            )
            print("\n==================================\n")

            dialog.append({"role": "assistant", "content": f"""\{result['generation']['content']}"""})



if __name__ == "__main__":
    print("Script loaded! \n")
    fire.Fire(main)
