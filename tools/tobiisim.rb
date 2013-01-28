#!/usr/bin/env ruby

#This script simulates the Tobii ClearView Trigger API server. This is useful
#when a Tobii device is not present while running the plugin.

require 'socket'

class QuestionResponse
  attr_reader :type, :transaction_id, :transaction_seq, :command, :errorcode,
              :data

  def initialize(client, type)
    #First, get fields with known sizes
    data = client.recv(4 * 6).unpack('NNNNNN')
    @type = data[0]
    @transaction_id = data[1]
    @transaction_seq = data[2]
    @command = data[3]
    @errorcode = data[4]
    datalen = data[5] #Not an instance attribute. String will store this
                      #information
    #Now get the data based on the data length
    @data = client.recv datalen

    #Verify correct package type
    if (type == :question and @type.chr != 'Q') or
       (type == :response and @type.chr != 'R')
      throw :type_mismatch
    end
  end

  def to_s
    return "QuestionResponse \##{self.object_id}\n" +
           " - Type: #{@type};\n" +
           " - Transaction ID: #{@transaction_id};\n" +
           " - Transaction Seq (Do not use): #{@transaction_seq};\n" +
           " - Command: #{@command};\n" +
           " - Error Code: #{@errorcode};\n" +
           " - Data Length: #{@errorcode}"
  end
end

puts "Starting server. Type Control+C at any time to end the server."
server = TCPServer.new 3780
loop do
  Thread.start server.accept do |client|
    puts "Begin client: #{client}"
    begin
      question = QuestionResponse.new(client, :question)
      puts question.to_s
    catch :type_mismatch
      puts "[Error] Type mismatch in question. Use 'Q'"
    end
    puts "End client: #{client}"
  end
end
