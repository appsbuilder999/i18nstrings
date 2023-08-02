# Translation Quality Assurance
### Assurance Measures

- **Incremental Translation Mechanism**: By focusing only on the missing translations and preserving the existing local translations, full translations are avoided each time, increasing efficiency.

- **Text Merging and Bulk Processing**: By merging text content for batch processing, not only is the translation speed significantly improved, but more context information is also provided for the texts, helping to enhance translation accuracy. The current translation engine uses AI for translation, providing more associated texts, aiding AI in deeply understanding the meaning of the texts.

- **Special Character Check**: For texts containing special characters (such as %d, %s, etc.), checks are performed after translation to ensure that inconsistencies in special characters will not cause the app to crash during operation.

- **Back-Translation and Calibration**: The translated content is back-translated (translated back into English), then compared with the original English text. Texts with significant differences are filtered out through similarity algorithms and retranslated one by one to ensure quality.

### Data Analysis

Using the Google Translate translation engine, tests were conducted on the open-source project AmazeFileManager (containing 660 texts), yielding the following data:

#### Translation Quality Analysis
- **Translate Percent**: The ratio of the total number of translated texts to the total number of English texts in the project.
- **Reverse Translate**: Back-translate the translated texts (retranslate them into English using the translation engine), and calculate the similarity percentage between the back-translated texts and the original English texts (similarity calculation is seen in TranslationManager.calSimilarityPercent).
- **Ref Translate**: The similarity percentage between the texts that the project itself already supports and the translated texts.
- **Ref Count Percent**: The ratio of the total number of texts that the project itself supports to the total number of English texts.

| **Code** | **Country**                   | **Translate Percent** | **Reverse Translate** | **Ref Translate** | **Ref Count Percent** |
|----------|-------------------------------|-----------------------|-----------------------|-------------------|-----------------------|
| he       | Hebrew                        | 95%                   | 93%                   | 86%               | 98%                   |
| de       | German                        | 91%                   | 95%                   | 74%               | 98%                   |
| it       | Italian                       | 93%                   | 95%                   | 85%               | 96%                   |
| vi       | Vietnamese                    | 96%                   | 92%                   | 84%               | 90%                   |
| hu       | Hungarian                     | 95%                   | 93%                   | 83%               | 90%                   |
| ar       | Arabic                        | 91%                   | 94%                   | 72%               | 90%                   |
| id       | Indonesian                    | 93%                   | 95%                   | 85%               | 90%                   |
| zh-TW    | Chinese (Traditional)         | 98%                   | 93%                   | 83%               | 89%                   |
| es       | Spanish                       | 95%                   | 95%                   | 87%               | 85%                   |
| fr       | French                        | 95%                   | 96%                   | 87%               | 84%                   |
| ja       | Japanese                      | 98%                   | 92%                   | 89%               | 83%                   |
| zh-CN    | Chinese (Simplified)          | 98%                   | 93%                   | 86%               | 83%                   |
| uk       | Ukrainian                     | 96%                   | 94%                   | 83%               | 82%                   |
| tr       | Turkish                       | 97%                   | 95%                   | 87%               | 81%                   |
| is       | Icelandic                     | 95%                   | 95%                   | 81%               | 80%                   |
| sv       | Swedish                       | 92%                   | 97%                   | 84%               | 77%                   |
| eu       | Basque                        | 96%                   | 95%                   | 87%               | 76%                   |
| eo       | Esperanto                     | 96%                   | 96%                   | 81%               | 67%                   |
| cs       | Czech                         | 95%                   | 94%                   | 80%               | 65%                   |
| ru       | Russian                       | 96%                   | 96%                   | 85%               | 65%                   |
| sk       | Slovak                        | 95%                   | 95%                   | 83%               | 65%                   |
| be       | Belarusian                    | 97%                   | 95%                   | 84%               | 63%                   |
| fi       | Finnish                       | 95%                   | 94%                   | 77%               | 63%                   |
| pl       | Polish                        | 96%                   | 96%                   | 80%               | 57%                   |
| ko       | Korean                        | 98%                   | 90%                   | 82%               | 54%                   |
| sl       | Slovenian                     | 96%                   | 95%                   | 85%               | 54%                   |
| nl       | Dutch                         | 92%                   | 97%                   | 79%               | 54%                   |
| ta       | Tamil                         | 95%                   | 94%                   | 72%               | 52%                   |
| pt       | Portuguese (Portugal, Brazil) | 94%                   | 96%                   | 80%               | 52%                   |
| as       | Assamese                      | 99%                   | 93%                   | 8%                | 51%                   |
| bg       | Bulgarian                     | 95%                   | 95%                   | 84%               | 50%                   |
| el       | Greek                         | 96%                   | 93%                   | 81%               | 49%                   |
| fa       | Persian                       | 95%                   | 93%                   | 71%               | 39%                   |
| bn       | Bengali                       | 96%                   | 92%                   | 29%               | 38%                   |
| ro       | Romanian                      | 93%                   | 95%                   | 85%               | 35%                   |
| lt       | Lithuanian                    | 96%                   | 95%                   | 83%               | 19%                   |
| da       | Danish                        | 91%                   | 98%                   | 80%               | 6%                    |
| ca       | Catalan                       | 95%                   | 95%                   | 90%               | 5%                    |
| az       | Azerbaijani                   | 95%                   | 93%                   | 76%               | 4%                    |
| af       | Afrikaans                     | 95%                   | 98%                   | 95%               | 4%                    |
| et       | Estonian                      | 96%                   | 93%                   | 90%               | 3%                    |
| hr       | Croatian                      | 95%                   | 95%                   | 89%               | 3%                    |
| th       | Thai                          | 97%                   | 90%                   | 89%               | 3%                    |
| hi       | Hindi                         | 98%                   | 93%                   | 88%               | 3%                    |
| lv       | Latvian                       | 96%                   | 95%                   | 83%               | 3%                    |
| si       | Sinhala (Sinhalese)           | 92%                   | 94%                   | 77%               | 3%                    |
| ms       | Malay                         | 95%                   | 95%                   | 94%               | 3%                    |
| mn       | Mongolian                     | 97%                   | 91%                   | 94%               | 3%                    |
| ka       | Georgian                      | 96%                   | 94%                   | 88%               | 3%                    |
| km       | Khmer                         | 95%                   | 90%                   | 88%               | 3%                    |
| zu       | Zulu                          | 95%                   | 95%                   | 88%               | 3%                    |
| am       | Amharic                       | 97%                   | 93%                   | 82%               | 3%                    |
| lo       | Lao                           | 94%                   | 93%                   | 82%               | 3%                    |
| sw       | Swahili                       | 96%                   | 94%                   | 82%               | 3%                    |
| hy       | Armenian                      | 96%                   | 96%                   | 76%               | 3%                    |
| ne       | Nepali                        | 94%                   | 94%                   | 70%               | 3%                    |
| tl       | Tagalog (Filipino)            | 84%                   | 97%                   | 80%               | 2%                    |
| my       | Myanmar (Burmese)             | 90%                   | 92%                   | 92%               | 2%                    |
| ml       | Malayalam                     | 97%                   | 95%                   | 84%               | 2%                    |
| gl       | Galician                      | 94%                   | 94%                   | 100%              | 2%                    |
| mr       | Marathi                       | 96%                   | 95%                   | 88%               | 1%                    |
| kn       | Kannada                       | 96%                   | 94%                   | 77%               | 1%                    |
| lb       | Luxembourgish                 | 86%                   | 97%                   | 77%               | 1%                    |
| gu       | Gujarati                      | 96%                   | 95%                   | 66%               | 1%                    |
| te       | Telugu                        | 97%                   | 94%                   | 55%               | 1%                    |
| uz       | Uzbek                         | 95%                   | 91%                   | 55%               | 1%                    |
| sq       | Albanian                      | 94%                   | 95%                   | 100%              | 1%                    |
| kk       | Kazakh                        | 97%                   | 91%                   | 100%              | 1%                    |
| mk       | Macedonian                    | 96%                   | 95%                   | 85%               | 1%                    |
| pa       | Punjabi                       | 96%                   | 93%                   | 85%               | 1%                    |
| ur       | Urdu                          | 92%                   | 94%                   | 85%               | 1%                    |
| ky       | Kyrgyz                        | 96%                   | 93%                   | 71%               | 1%                    |
| ug       | Uyghur                        | 88%                   | 87%                   | 100%              | 1%                    |
| or       | Odia (Oriya)                  | 97%                   | 92%                   | 50%               | 0%                    |
| ku       | Kurdish                       | 96%                   | 88%                   | 0%                | 0%                    |
| ay       | Aymara                        | 97%                   | 87%                   | 0%                | 0%                    |
| bm       | Bambara                       | 91%                   | 84%                   | 0%                | 0%                    |
| bho      | Bhojpuri                      | 99%                   | 95%                   | 0%                | 0%                    |
| bs       | Bosnian                       | 92%                   | 95%                   | 0%                | 0%                    |
| ceb      | Cebuano                       | 81%                   | 96%                   | 0%                | 0%                    |
| co       | Corsican                      | 84%                   | 95%                   | 0%                | 0%                    |
| dv       | Dhivehi                       | 99%                   | 94%                   | 0%                | 0%                    |
| doi      | Dogri                         | 99%                   | 94%                   | 0%                | 0%                    |
| ee       | Ewe                           | 98%                   | 88%                   | 0%                | 0%                    |
| fil      | Filipino (Tagalog)            | 84%                   | 97%                   | 0%                | 0%                    |
| fy       | Frisian                       | 90%                   | 97%                   | 0%                | 0%                    |
| gn       | Guarani                       | 96%                   | 86%                   | 0%                | 0%                    |
| ht       | Haitian Creole                | 94%                   | 93%                   | 0%                | 0%                    |
| ha       | Hausa                         | 88%                   | 89%                   | 0%                | 0%                    |
| haw      | Hawaiian                      | 93%                   | 85%                   | 0%                | 0%                    |
| hmn      | Hmong                         | 86%                   | 85%                   | 0%                | 0%                    |
| ig       | Igbo                          | 90%                   | 84%                   | 0%                | 0%                    |
| ilo      | Ilocano                       | 95%                   | 95%                   | 0%                | 0%                    |
| ga       | Irish                         | 96%                   | 94%                   | 0%                | 0%                    |
| jv       | Javanese                      | 91%                   | 94%                   | 0%                | 0%                    |
| rw       | Kinyarwanda                   | 94%                   | 83%                   | 0%                | 0%                    |
| gom      | Konkani                       | 99%                   | 89%                   | 0%                | 0%                    |
| kri      | Krio                          | 95%                   | 97%                   | 0%                | 0%                    |
| ckb      | Kurdish (Sorani)              | 98%                   | 91%                   | 0%                | 0%                    |
| la       | Latin                         | 70%                   | 95%                   | 0%                | 0%                    |
| ln       | Lingala                       | 96%                   | 91%                   | 0%                | 0%                    |
| lg       | Luganda                       | 97%                   | 89%                   | 0%                | 0%                    |
| mai      | Maithili                      | 99%                   | 94%                   | 0%                | 0%                    |
| mg       | Malagasy                      | 90%                   | 90%                   | 0%                | 0%                    |
| mt       | Maltese                       | 89%                   | 96%                   | 0%                | 0%                    |
| mi       | Maori                         | 97%                   | 90%                   | 0%                | 0%                    |
| mni-Mtei | Meiteilon (Manipuri)          | 99%                   | 93%                   | 0%                | 0%                    |
| lus      | Mizo                          | 98%                   | 85%                   | 0%                | 0%                    |
| no       | Norwegian                     | 93%                   | 99%                   | 0%                | 0%                    |
| ny       | Nyanja (Chichewa)             | 91%                   | 86%                   | 0%                | 0%                    |
| om       | Oromo                         | 97%                   | 88%                   | 0%                | 0%                    |
| ps       | Pashto                        | 94%                   | 90%                   | 0%                | 0%                    |
| qu       | Quechua                       | 97%                   | 87%                   | 0%                | 0%                    |
| sm       | Samoan                        | 93%                   | 80%                   | 0%                | 0%                    |
| sa       | Sanskrit                      | 98%                   | 92%                   | 0%                | 0%                    |
| gd       | Scots Gaelic                  | 93%                   | 93%                   | 0%                | 0%                    |
| nso      | Sepedi                        | 98%                   | 93%                   | 0%                | 0%                    |
| st       | Sesotho                       | 91%                   | 91%                   | 0%                | 0%                    |
| sn       | Shona                         | 85%                   | 91%                   | 0%                | 0%                    |
| sd       | Sindhi                        | 89%                   | 93%                   | 0%                | 0%                    |
| so       | Somali                        | 88%                   | 92%                   | 0%                | 0%                    |
| su       | Sundanese                     | 93%                   | 95%                   | 0%                | 0%                    |
| tg       | Tajik                         | 97%                   | 92%                   | 0%                | 0%                    |
| tt       | Tatar                         | 96%                   | 90%                   | 0%                | 0%                    |
| ti       | Tigrinya                      | 98%                   | 89%                   | 0%                | 0%                    |
| ts       | Tsonga                        | 97%                   | 92%                   | 0%                | 0%                    |
| tk       | Turkmen                       | 95%                   | 87%                   | 0%                | 0%                    |
| ak       | Twi (Akan)                    | 93%                   | 89%                   | 0%                | 0%                    |
| cy       | Welsh                         | 97%                   | 95%                   | 0%                | 0%                    |
| xh       | Xhosa                         | 94%                   | 92%                   | 0%                | 0%                    |
| yi       | Yiddish                       | 96%                   | 98%                   | 0%                | 0%                    |
| yo       | Yoruba                        | 92%                   | 90%                   | 0%                | 0%                    |


#### Translation Speed (Macbook Pro 14-inch, M1 Pro)
| **Code** | **Country**                   | **Cost Time(ms)** | **Translate Percent** | **Translate Count** |
|----------|-------------------------------|-------------------|-----------------------|---------------------|
| hr       | Croatian                      | 29502             | 95%                   | 629                 |
| af       | Afrikaans                     | 30475             | 95%                   | 628                 |
| nl       | Dutch                         | 31722             | 93%                   | 611                 |
| vi       | Vietnamese                    | 31763             | 96%                   | 634                 |
| bg       | Bulgarian                     | 31930             | 95%                   | 629                 |
| da       | Danish                        | 32375             | 92%                   | 604                 |
| id       | Indonesian                    | 32416             | 93%                   | 617                 |
| ca       | Catalan                       | 32670             | 95%                   | 628                 |
| fy       | Frisian                       | 32693             | 91%                   | 599                 |
| sv       | Swedish                       | 32752             | 93%                   | 613                 |
| cs       | Czech                         | 32769             | 96%                   | 631                 |
| he       | Hebrew                        | 33012             | 96%                   | 633                 |
| kri      | Krio                          | 33345             | 96%                   | 631                 |
| hu       | Hungarian                     | 33733             | 96%                   | 631                 |
| pt       | Portuguese (Portugal, Brazil) | 33833             | 94%                   | 621                 |
| is       | Icelandic                     | 33898             | 96%                   | 631                 |
|          | Malay                         | 34050             | 95%                   | 630                 |
| sl       | Slovenian                     | 34140             | 96%                   | 636                 |
| no       | Norwegian                     | 34242             | 93%                   | 617                 |
| lo       | Lao                           | 34286             | 94%                   | 622                 |
| su       | Sundanese                     | 34833             | 93%                   | 616                 |
| gom      | Konkani                       | 35226             | 100%                  | 658                 |
| ts       | Tsonga                        | 35284             | 98%                   | 645                 |
| fr       | French                        | 35508             | 95%                   | 629                 |
| ro       | Romanian                      | 36172             | 93%                   | 615                 |
| ay       | Aymara                        | 36769             | 97%                   | 641                 |
| ht       | Haitian Creole                | 36777             | 94%                   | 622                 |
| de       | German                        | 36904             | 91%                   | 601                 |
| bho      | Bhojpuri                      | 36931             | 99%                   | 655                 |
| zh-TW    | Chinese (Traditional)         | 37511             | 98%                   | 649                 |
| be       | Belarusian                    | 37905             | 98%                   | 646                 |
| bm       | Bambara                       | 38123             | 92%                   | 604                 |
| mk       | Macedonian                    | 38193             | 97%                   | 640                 |
| gl       | Galician                      | 39247             | 95%                   | 624                 |
| ilo      | Ilocano                       | 39370             | 95%                   | 628                 |
|          | Malay                         | 39529             | 95%                   | 630                 |
| sk       | Slovak                        | 39544             | 95%                   | 629                 |
| el       | Greek                         | 40770             | 96%                   | 635                 |
| it       | Italian                       | 40886             | 93%                   | 614                 |
| eo       | Esperanto                     | 41124             | 97%                   | 640                 |
| es       | Spanish                       | 41442             | 96%                   | 632                 |
| sq       | Albanian                      | 42427             | 94%                   | 623                 |
| tr       | Turkish                       | 42544             | 98%                   | 644                 |
| lv       | Latvian                       | 42611             | 97%                   | 640                 |
| jv       | Javanese                      | 43219             | 92%                   | 607                 |
| ckb      | Kurdish (Sorani)              | 43371             | 99%                   | 652                 |
| bs       | Bosnian                       | 43623             | 92%                   | 608                 |
| sw       | Swahili                       | 44267             | 97%                   | 640                 |
| nso      | Sepedi                        | 44482             | 98%                   | 647                 |
| kk       | Kazakh                        | 45236             | 97%                   | 642                 |
| ln       | Lingala                       | 45976             | 96%                   | 634                 |
| uk       | Ukrainian                     | 46000             | 97%                   | 638                 |
| ru       | Russian                       | 47498             | 96%                   | 634                 |
| eu       | Basque                        | 47781             | 96%                   | 634                 |
| lb       | Luxembourgish                 | 48235             | 86%                   | 569                 |
| hy       | Armenian                      | 48526             | 96%                   | 634                 |
| qu       | Quechua                       | 49451             | 97%                   | 643                 |
| ko       | Korean                        | 50051             | 98%                   | 648                 |
| zh-CN    | Chinese (Simplified)          | 50362             | 98%                   | 647                 |
| ti       | Tigrinya                      | 50809             | 99%                   | 651                 |
| fi       |  Filipino (Tagalog)           | 51077             | 84%                   | 555                 |
| mt       | Maltese                       | 52424             | 89%                   | 590                 |
| pl       | Polish                        | 52847             | 96%                   | 635                 |
| mi       | Maori                         | 52890             | 98%                   | 644                 |
| mk       | Macedonian                    | 53252             | 97%                   | 640                 |
| tl       | Tagalog (Filipino)            | 53488             | 84%                   | 555                 |
| mn       | Mongolian                     | 54246             | 97%                   | 641                 |
| ee       | Ewe                           | 54377             | 98%                   | 648                 |
| ak       | Twi (Akan)                    | 54416             | 93%                   | 614                 |
| az       | Azerbaijani                   | 56616             | 95%                   | 627                 |
| as       | Assamese                      | 57070             | 99%                   | 656                 |
| zu       | Zulu                          | 58811             | 96%                   | 631                 |
| ja       | Japanese                      | 59270             | 98%                   | 649                 |
| mt       | Maltese                       | 59629             | 89%                   | 590                 |
| ky       | Kyrgyz                        | 60332             | 96%                   | 634                 |
| fi       | Finnish                       | 60385             | 96%                   | 632                 |
| tg       | Tajik                         | 61043             | 98%                   | 645                 |
| gn       | Guarani                       | 61924             | 97%                   | 640                 |
| te       | Telugu                        | 62089             | 98%                   | 645                 |
| doi      | Dogri                         | 62102             | 99%                   | 656                 |
| et       | Estonian                      | 62376             | 96%                   | 635                 |
| haw      | Hawaiian                      | 62644             | 94%                   | 620                 |
| dv       | Dhivehi                       | 62908             | 100%                  | 658                 |
| mni-Mtei | Meiteilon (Manipuri)          | 63023             | 99%                   | 654                 |
| bn       | Bengali                       | 64204             | 97%                   | 639                 |
| cy       | Welsh                         | 64234             | 97%                   | 641                 |
| ceb      | Cebuano                       | 64313             | 82%                   | 541                 |
| ku       | Kurdish                       | 64798             | 96%                   | 634                 |
| mi       | Maori                         | 65261             | 98%                   | 644                 |
| th       | Thai                          | 65569             | 97%                   | 641                 |
| st       | Sesotho                       | 67829             | 92%                   | 607                 |
| uz       | Uzbek                         | 67920             | 95%                   | 630                 |
| lb       | Luxembourgish                 | 70593             | 86%                   | 569                 |
| mg       | Malagasy                      | 72055             | 90%                   | 597                 |
| om       | Oromo                         | 73098             | 98%                   | 645                 |
| gd       | Scots Gaelic                  | 73649             | 93%                   | 617                 |
| my       | Myanmar (Burmese)             | 74502             | 90%                   | 596                 |
| yi       | Yiddish                       | 75058             | 97%                   | 637                 |
| pa       | Punjabi                       | 76842             | 96%                   | 636                 |
| lg       | Luganda                       | 77874             | 98%                   | 646                 |
| lt       | Lithuanian                    | 78445             | 97%                   | 639                 |
| mg       | Malagasy                      | 83360             | 90%                   | 597                 |
| co       | Corsican                      | 83558             | 84%                   | 555                 |
| ar       | Arabic                        | 83599             | 92%                   | 604                 |
| mr       | Marathi                       | 84062             | 97%                   | 637                 |
| gu       | Gujarati                      | 85689             | 97%                   | 637                 |
| ny       | Nyanja (Chichewa)             | 88420             | 91%                   | 601                 |
| fa       | Persian                       | 89002             | 95%                   | 627                 |
| ga       | Irish                         | 90110             | 96%                   | 636                 |
| ne       | Nepali                        | 90427             | 94%                   | 623                 |
| sm       | Samoan                        | 91597             | 93%                   | 617                 |
| yo       | Yoruba                        | 94472             | 93%                   | 613                 |
| hi       | Hindi                         | 106162            | 99%                   | 652                 |
| am       | Amharic                       | 107273            | 98%                   | 644                 |
| ka       | Georgian                      | 108391            | 96%                   | 635                 |
| sa       | Sanskrit                      | 113264            | 98%                   | 647                 |
| ml       | Malayalam                     | 118015            | 97%                   | 641                 |
| ur       | Urdu                          | 118772            | 92%                   | 609                 |
| kn       | Kannada                       | 121082            | 97%                   | 640                 |
| ml       | Malayalam                     | 131208            | 97%                   | 641                 |
| km       | Khmer                         | 137183            | 96%                   | 632                 |
| ta       | Tamil                         | 139776            | 95%                   | 627                 |
| ps       | Pashto                        | 141084            | 94%                   | 622                 |
| si       | Sinhala (Sinhalese)           | 213492            | 92%                   | 610                 |
| sn       | Shona                         | 256393            | 86%                   | 565                 |
| xh       | Xhosa                         | 267257            | 94%                   | 621                 |
| ig       | Igbo                          | 271981            | 90%                   | 594                 |
| mai      | Maithili                      | 276291            | 99%                   | 655                 |
| ha       | Hausa                         | 291560            | 89%                   | 587                 |
| mai      | Maithili                      | 299707            | 99%                   | 655                 |
| sd       | Sindhi                        | 327620            | 90%                   | 591                 |
| so       | Somali                        | 412762            | 88%                   | 582                 |
| la       | Latin                         | 462574            | 70%                   | 463                 |
| tt       | Tatar                         | 467488            | 97%                   | 637                 |
| la       | Latin                         | 530171            | 70%                   | 463                 |
| or       | Odia (Oriya)                  | 583930            | 98%                   | 646                 |
| tk       | Turkmen                       | 606506            | 96%                   | 633                 |
| hmn      | Hmong                         | 755872            | 86%                   | 570                 |
| rw       | Kinyarwanda                   | 766956            | 95%                   | 624                 |
| ug       | Uyghur                        | 777212            | 89%                   | 585                 |
| lus      | Mizo                          | 835548            | 98%                   | 649                 |
| rw       | Kinyarwanda                   | 1118985           | 95%                   | 624                 |
